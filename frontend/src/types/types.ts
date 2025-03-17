export type Message = {
    id: string;
    text: string;
    type: "Sent" | "Received";
};

export type NLURequest = {
    utterance: string,
    sessionid: string | null,
}

export type NLUResponse = {
    intent: string,
}

export type NLGRequest = {
    subject: string,
    attribute: string,
    utterance: string,
}

export type NLGResponse = {
    message: string,
}

export type ParsedIntent = {
    subject: string;
    attribute: string | null;
};

export type RegisterRequest = {
    username: string;
    password: string;
    email: '';
    role: Role.LEARNER;
    code: '';

}

export enum Role {
    LEARNER= "LEARNER",
    TRAINER = "TRAINER",
    ADMIN = "ADMIN",
}