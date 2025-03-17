# Imports
import json
import yaml
import faiss
import requests
from sentence_transformers import SentenceTransformer
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import axios

# Preload embedding_model
embedding_model = SentenceTransformer('all-MiniLM-L6-v2')

# Ollama API metadata
url = "http://lilobottest.ewi.tudelft.nl:11434/api/generate"
headers = {
    "Content-Type": "application/json"
}

# NLG Service URL
nlg_url = "http://lilobottest.ewi.tudelft.nl/nlg/dialogue_generation"

# BDI API metadata
bdi_url = "http://lilobottest.ewi.tudelft.nl/bdi/app/chat/"

# Service
nlu_service = FastAPI()

nlu_service.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Defining the request structure
class NLURequest(BaseModel):
    utterance: str
    sessionid: str


# Defining the response structure
class NLUResponse(BaseModel):
    intent: str


# Defining the request structure
class BDIRequest(BaseModel):
    type: str
    subject: str
    attribute: str
    text: str


# Defining the request structure
class NLGRequest(BaseModel):
    sessionid: str
    subject: str
    attribute: str
    utterance: str
    knowledgename: str


# Defining the response structure
class NLGResponse(BaseModel):
    sessionid: str
    message: str


def load_intents(path):
    # Open the file
    with open(path, 'r') as file:
        intents_yml = yaml.safe_load(file)

    # Save each intent and example pair as a dictionary into a large array as {"intent": <intent>, "example": <example>}
    only_intents = []
    intents = []
    for item in intents_yml:
        intent = item['intent']
        only_intents.append(intent)
        examples = [example.strip('- ').strip() for example in item['examples'].splitlines() if example.strip()]
        for example in examples:
            intents.append({"intent": intent, "example": example})
    return intents, only_intents


def create_embeddings(intents):
    # Extract just the embeddings
    examples = [item["example"] for item in intents]
    # Creat embeddings from examples
    embeddings = embedding_model.encode(examples, convert_to_numpy=True)
    return embeddings


def create_vector_db(embeddings):
    # Get the dimensions
    dimensions = embeddings.shape[1]
    # Use L2 (Euclidean distance) as the distance measure for the vector database
    vdb = faiss.IndexFlatL2(dimensions)
    vdb.add(embeddings)
    return vdb


def get_k_closest(utterance, intents, vector_db, k=5):
    # Get the embedding of the utterance
    utterance_embedding = embedding_model.encode([utterance], convert_to_numpy=True)
    # Search the vector database
    distances, indices = vector_db.search(utterance_embedding, k)
    # Map indices to intents and examples
    results = results = [{"intent": intents[i]["intent"], "example": intents[i]["example"], "distance": distances[0][j]}
                         for j, i in enumerate(indices[0])]
    return results


def llm_call(utterance, best_matches):
    # Prompt
    prompt = f"Classify the intent of this utterance: '{utterance}'. The possible intents and examples are:\n"
    for i, match in enumerate(best_matches):
        prompt += f"{match['intent']} (e.g., '{match['example']}')\n"
    prompt += "If none of the options closely match the input utterance, return 'unknown'." \
              "Return ONLY the identified intent. No added notes, no explanations."

    print(prompt)

    # Request payload
    data = {
        "model": "llama3.2",
        "prompt": prompt,
        "stream": False
    }
    # Send API request
    response = requests.post(url, headers=headers, data=json.dumps(data))

    if response.status_code == 200:
        response_text = response.text
        data = json.loads(response_text)
        response_final = data["response"]
        return response_final
    else:
        print("Error:", response.status_code, response.text)
        return None


# Preload
intents, intent_list = load_intents('intents.yml')
embeddings = create_embeddings(intents)
vector_db = create_vector_db(embeddings)


# Set up NLU component as a service
# Set up the endpoint
@nlu_service.post("/intent_recognition", response_model=NLUResponse)
async def intent_recognition(request: NLURequest):
    print("NLU: NLU Service Called!")
    best_matches = get_k_closest(request.utterance, intents, vector_db)
    identified_intent = llm_call(request.utterance, best_matches)

    # If the intent is not in the list, explicitly make it 'unknown'
    if identified_intent in intent_list:
        print(identified_intent)

        # Split the intent into Type / Subject / Attribute
        intent_split = identified_intent.split('_')
        # Data for BDI API service
        data = BDIRequest(type=intent_split[0], subject=intent_split[1], attribute=intent_split[2], text=request.utterance)
        # Response data
        print("SENDING BDI CALL")
        response = requests.post(f'http://lilobottest.ewi.tudelft.nl/bdi/agent/{request.sessionid}', json=data.dict())

        if response.status_code == 200:
            print("BDI CALL WENT OK")
            response_final = response.json().get("message", "No message found")
            print(response_final)
        else:
            print("Error:", response.status_code, response.text)

        return NLUResponse(intent=identified_intent)

    else:
        print(f"unknown, ({identified_intent})")
        # Data for NLG service request
        data = NLGRequest(sessionid=request.sessionid, subject="unknown", attribute="", utterance=request.utterance, knowledgename="")
        # Response data
        response = requests.post(nlg_url, json=data.dict())

        if response.status_code == 200:
            response_final = response.json().get("message", "No message found")
            print(response_final)
        else:
            print("Error:", response.status_code, response.text)
        return NLUResponse(intent="unknown")

