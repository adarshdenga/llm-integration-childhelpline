# Imports
import json
import csv
import requests
from fastapi import FastAPI, WebSocket
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import asyncio

from starlette.websockets import WebSocketDisconnect

savedknowledgename = ""
knowledgebase = None
name = ""
goal = ""
location = ""


characters = {
    "knowledge_Ava_socialMedia_hit_back.csv": [
        "Ava",
        "on social media / online",
        "to get revenge by hitting the bullies back"
    ],
    "knowledge_Axel_neighberhood_call_school.csv": [
        "Axel",
        "in the neighborhood",
        "to call the bullies' parents"
    ],
    "knowledge_Daisy_schoolclass_stop_going.csv": [
        "Daisy",
        "in class",
        "to stop going to school to avoid the bullies"
    ],
    "knowledge_playdate_dania_no_goal.csv": [
        "Dania",
        "during a playdate",
        "no goal"
    ],
    "knowledge_soccer_Jim_no_goal.csv": [
        "Jim",
        "at soccer club",
        "no goal"
    ],
    "knowledge_Kai_onlinegaming_stop_playing.csv": [
        "Kai",
        "while online gaming",
        "to stop playing online games to avoid the bullies"
    ],
    "knowledge_Lilo_schoolbreak_call_school.csv": [
        "Lilo",
        "during school break",
        "to get the helpline to call school"
    ],
    "knowledge_home_Lyla_no_goal.csv": [
        "Lyla",
        "at home",
        "no goal"
    ],
    "knowledge_mayabot_music_good_goal.csv": [
        "Maya",
        "in music class",
        "you have some idea about a goal"
    ],
    "knowledge_Miles_schoolbreak_good_goal.csv": [
        "Miles",
        "during school break",
        "you have some idea about a goal"
    ],
    "knowledge_Ori_neighberhood_good_goal.csv": [
        "Ori",
        "in the neighborhood",
        "you have some idea about a goal"
    ],
    "knowledge_Tim_soccer_hit_back.csv": [
        "Tim",
        " at soccer club",
        "to hit the bullies back"
    ]
}


# Ollama API metadata
url = "http://lilobottest.ewi.tudelft.nl:11434/api/generate"
headers = {
    "Content-Type": "application/json"
}

# Current open websocket connections
active_connections = []

# Service
nlg_service = FastAPI()

nlg_service.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


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


def load_knowledgebase(path):
    # Open the file
    with open(path, 'r') as file:
        kb = csv.reader(file)
        # Save the knowledge base a dictionary
        knowledgebase = {}
        for row in kb:
            subject, attribute, *examples = row
            knowledgebase[(subject, attribute)] = examples

    return knowledgebase


def set_character(knowledgename):
    global name, location, goal
    name, location, goal = characters[knowledgename]


def get_examples(intent, knowledgebase):
    return knowledgebase[intent]


def llm_call(intent, utterance, examples):
    global name, goal, location

    prompt = f"You must play the character of {name}, a 9 year old child being bullied {location}." \
             f"Your current goal is - {goal}." \
             "You are talking to a child helpline counselor.\n"

    # Change the prompt based on whether the intent is known not
    if intent == 'unknown':
        # Establish input format
        prompt += "You will receive as input: \n" \
                  "- The counselor's message \n"
        # Embed input
        prompt += f"Counselor message: {utterance}\n"
        # Establish task
        prompt += "Generate a response to the counselor's message given the context provided. \n" \
                  "Return only the generated response. No added notes, no explanations."
    else:
        # Establish input format
        prompt += "You will receive as input: \n " \
                  "- The counselor's message \n " \
                  "- A set of example messages to respond with \n"
        # Embed input
        prompt += f"Counselor message: {utterance}\n"
        prompt += f"Examples:\n"
        for example in examples:
            prompt += f"- {example} \n"
        # Establish task
        prompt += "Generate a response to the counselor's message similar to the given examples. \n" \
                  "Return only the generated response. No added notes, no explanations."

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


# Set up NLG component as a service
# Set up the endpoint
@nlg_service.post("/dialogue_generation", response_model=NLGResponse)
async def dialogue_generation(request: NLGRequest):
    localsessionid = request.sessionid
    print("The session id is:", localsessionid)
    global savedknowledgename, knowledgebase

    if request.knowledgename and request.knowledgename != savedknowledgename:
        savedknowledgename = request.knowledgename
        knowledgebase = load_knowledgebase('knowledge/' + request.knowledgename)
        set_character(request.knowledgename)

    print("NLG Service Called!")
    if request.subject == 'unknown':
        response = llm_call(intent='unknown', utterance=request.utterance, examples=[])
    else:
        intent = (request.subject, request.attribute)
        examples = get_examples(intent, knowledgebase)
        response = llm_call(intent="", utterance=request.utterance, examples=examples)

    async def safe_task():
        try:
            await push_message_to_frontend(localsessionid, response.strip('"'))
        except Exception as e:
            print(f"Error in websocket task: {e}")

    asyncio.create_task(safe_task())

    return NLGResponse(sessionid=localsessionid, message=response)


# Websocket endpoint
@nlg_service.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    active_connections.append(websocket)

    try:
        while True:
            await asyncio.sleep(1)
    except Exception as e:
        print(f"WebSocket error: {e}")
    finally:
        # Remove the connection when disconnected
        if websocket in active_connections:
            active_connections.remove(websocket)


# Send the message back to the frontend
async def push_message_to_frontend(localsessionid: str, message: str):
    disconnected_connections = []
    for connection in active_connections:
        try:
            await connection.send_text(json.dumps({"sessionid":localsessionid, "message": message}))
        except WebSocketDisconnect:
            print(f"WebSocket disconnected. Removing connection.")
            disconnected_connections.append(connection)
        except Exception as e:
            print(f"Error sending WebSocket message: {e}")
            disconnected_connections.append(connection)

    # Remove disconnected connections from the active list
    for connection in disconnected_connections:
        active_connections.remove(connection)
