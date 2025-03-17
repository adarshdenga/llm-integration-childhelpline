## Purpose statement

This document provides deeper insight into the **backend** implementation of the Optimal Path Algorithm, its assumptions/restrictions, and which aspects need to be considered when making changes to the underlying model. This is directed at future developers working on the application.

## Overview of classes

|Class name|Folder|Responsibilty|
|--|--|--|
|`OptimalPathController`|`api`|Endpoint for getting the optimal path model. Calls method in the service, and returns result.|
|`GraphUpdatingService`|`service/graph`|Updating the nodes and edges of the graph. Contains functionality for updating belief values, and trying to get closer to get a constraint on a belief. This service is *highly* coupled to the current implementation.|
|`GraphUtilsService`|`service/graph`|General utility methods for the other path/graph services. E.g., getting the active desire from the belief values.|
|`OptimalPathService`|`service/graph`|Handles the generation of the graph, and the finding of the optimal path in that graph. Takes an agent, and generates from the current state of it.|
|`ConstraintProvider`|`utils`|Provides constraints for desires and for phase transitions.|
|`FloatComparer`|`utils`|A service used to safely compare float values, using a pre-configured epsilon (difference). The epsilon value can be set in the `ApplicationConfig`.|
|`OptimalPathModel`|`model/api`|Model returned by the endpoint. Contains a `List` of `MessageNodeDto`s.|
|`BeliefDto`|`model/dto`|DTO for a belief. Contains only the information needed for the path frontend (name, full name, value).|
|`DesireDto`|`model/dto`|DTO for a desire. Contains only the information needed for the path frontend (name, full name, whether it is active).|
|`IntentDto`|`model/dto`|DTO for a user intent. Contains the name of the intent, and an example message (hardcoded from the nlu file).|
|`MessageNodeDto`|`model/dto`|DTO for a message (node). Contains a list of `BeliefDto`s, a list of `desireDto`s, the `Phase`, and an `IntentDto` (edge). The edge is `null` if it is the last edge in the path. The values of the attributes describe the agents state at that message.|
|`BeliefName`|`model/enums`|The names of the beliefs, currently `B1` to `B17`.|
|`DesireName`|`model/enums`|The names of the desires, currently `D1` to `D5`.|
|`BoundaryCheck`|`model/enums`|Used for constraints, and to check if a constraint is satisfied to compare float values. Values: `LEQ (<=)`, `GEQ (>=)`, `LT (<)`, `GT (>)`, `EQ (=)`, `NEQ (!=)`.|
|`Phase`|`model/enums`|Describes phase in five phase model: `PHASE1`, `PHASE2`, `PHASE3`, `PHASE4`, `PHASE5`.|
|`GraphEdge`|`model/graph`|Model for an edge in the generated graph. This is different than the model sent to the frontend, since it contains additional information. In  addition to the intention name, it also includes: Type, subject, attribute, node from which it comes, and node to which it connects.|
|`GraphNode`|`model/graph`|Model for a node in the generated graph, describes the inner state of the agent at that point. Contains the belief values, the edges to which it connects, the phase and the current subject.|
|`PhaseTransitionConstraints`|`model/util`|Describes constraints for transitioning from a phase to the next one. More concrete information will be provided in the implementation details below. Generally, it contains the phases (from/to), constraint goals, impossible to transition constraints, the optimal constraints (if any), and example belief values for the phase (from).|
|`BeliefConstraint`|`model/util`|Describes a constraint/condition of a belief: the `BoundaryCheck` describing the boundary, the name of the belief and the goal value to reach (i.e., if it is reached, the constraint is satisfied).|

## General data flow in model

The following describes how data flows from user input to internal updates to the state, in the initial model.

1. Rasa maps user input to an intent (only considering first sentence)
2. The intent name is split, and a perception is created from this information (type, subject, attribute, text)
3. This is sent to the `AgentController`, which handles it, and forwards it to the `AgentService` `reason` method
4. If its a `trigger`, the agent is prompted to say something to fulfill a currently uncompleted action connected to the active desire
5. If the subject of the perception is `unknown`, it is parsed to be the current active subject of the agent
6. Beliefs are updated considering the internal state of the agent, as well as the parsed perception
7. Desires are updated according to belief values, and the intention is set to the first active desire according to the desire ordering (ordering by ID: D1 -> D2 -> D3 -> D4 -> D5)
8. Intention is updated: Set current subject to perceptions subject, get intention/desire, if the agent wants to end the conversation, it returns this, otherwise it tries to retrieve the answer from the knowledge data according to the perceptions attributes
9. The response is returned & sent as answer to the request in `AgentController`

### Changes to initial model

Contradicting the thesis, the active desire (i.e., intention) was not set to the desire in the correct ordering mentioned above. The desires were a `Set` connected to the agent, and were unordered, thus making the intention be a random one from the active desires. This was changed to match the description in the thesis, since otherwise the randomness would lead to unpredictability. We assumed this to be an unintentional mistake in the initial model. The algorithm also assumes desires to be considered in the order of their ID.

One set of constraints for a desire was changed, since there was an inconsistency with the thesis. Namely, the check for `B2 == 1` was not included for `D2` (phase 5) in the code. In the thesis, this is described as the optimal outcome of phase 5. Thus, we added this constraint, and `D2` is active if `B2 == 1`. Since it is the optimal outcome, this is also the only constraint considered in the optimal path algorithm, which will be described in more detail later.

The methods in the `agentService` now also use the `floatComparer`, the simple comparison of float values lead to unintentional errors before, where for example, `1.00001 != 1` and therefore a desire was not activated. It also uses the provided desire constraints using the `constraintProvider` and checks the activity of desires with the `constraintService`.

## Phases transitions, desires and beliefs

Currently, there are 5 desires, and 17 beliefs. The exact belief/desire names can be found in the files (sub folder `files` in `dktagent`).

Each phase corresponds to one or more desires. The desire considered for the phase is the first active one (i.e., the intention). This is the mapping of desires to phases:

|Phase|Desire(s)|
|--|--|
|Phase 1|None active|
|Phase 2|D1|
|Phase 3|D3 or D5|
|Phase 4|D4|
|Phase 5|D2|

Thus this is how the agent go through the five phases optimally: **Start (no desire) => D1 => D3/D5 => D4 => D2**. The following sections lay out the conditions on the belief values for each phase. Note that the belief descriptions below are simplified, for more details check the thesis and the files in the repository. Also, the values are the ones currently specified by the `minValue`, `maxValue`, `minThreshold`,  `midThreshold` and `maxThreshold` variables that can now be modified in the setup file.

#### From phase 1 to phase 2
=> *Goal*: Activate D1

- **Activate D1**: `B3 > 0.3 && B9 < 0.7 && B10 = 0 && B12 = 0`
	- *B3*: Connected to KT
	- *B9*: Has talked about his situation
	- *B10*: KT is asking about his wish
	- *B12*: Asking about a confidant

#### From phase 2 to phase 3
=> *Goal*: Activate D3 *or* D5

=> *Goal*: Do **not** activate D1, D2, (D4 if only D5 is active)

- **Activate D3**: `B2 < 0.7 && B8 > 0.7 && B10 = 1`
	- *B2*: Competent to solve the problem
	- *B8*: KT can solve his problem
	- *B10*: KT is asking about his wish

- **Activate D5**: `B4 >= 0.5 && B10 = 1 && B12 = 0`
	- *B4*: KT can be trusted
	- *B10*: KT is asking about his wish
	- *B12*: Asking about a confidant

#### From phase 3 to phase 4
=> *Goal*: Activate D4

=> *Goal*: Do **not** activate D1, D2, D3

- **Activate D4**: `B3 >= 0.5 && B12 = 1 && B13 >= 0.5`
	- *B3*: Connected to KT
	- *B12*: Asking about a confidant
	- *B13*: Feels safe

#### From phase 4 to phase 5
=> *Goal*: Activate D2
=> *Goal*: Do **not** activate D1

- **Activate D2**: `B1 < 0.3 || B17 = 1 || (B3 < 0.5 && B12 = 1) || B2 = 1`
	- *B1*: LB is in control
	- *B17*: KT is going to solve the problem
	- *B3*: Connected to KT
	- *B12*: Asking about a confidant
	- *B2*: Competent to solve the problem

=> **B2 = 1 is the optimal outcome.**

## Implementation of the algorithm

### General idea/stages of algorithm

In the following, the general idea and stages of the algorithm will be laid out. The goal is to find the shortest possible conversation to get the agent from the current phase to the fifth phase of the conversation model. No phase should be skipped, and there should be no backward transitions in the phases (for example, phase 2 to phase 1). Overall, the algorithm is composed of two stages:

1. Creating a graph of possible user intents (edges), and the impact on the agents internal state (nodes)
2. Using the generated graph, finding the optimal (shortest) path to get from the current phase to phase 5

#### Graph generation algorithm

The general idea is to generate edges (user intents) leading to nodes (agents internal state), from the current state of the agent to check which interactions can be possible. The search space (i.e., which user intents are considered), needs to be limited since otherwise the generation will take an indefinite amount of space and time (if every user intent would be considered from every node). For this, it is however not considered which intents belong to which phase explicitly (which is partially indicated in the nlu file), but by an indirect manner of considering the beliefs per phase and which intents influence those beliefs. Intents are considered for a phase, if they influence a belief which is part of the constraints of the next phase, to be closer or equal to its goal value. These interactions and constraints are highly coupled to the initial implementation, and will need adaptations if the model is changed.

Generally, an extensive graph (considering the above constraints and restricted search space) is generated. However, there are some specific scenarios which require additional manual restrictions. These will be explained in the section on "Restrictions and assumptions of the algorithm".

Each belief (if it is modifiable) has a corresponding method in the `GraphUpdatingService` called `updateB(X)` (`X` being the belief number). The methods take the current node, the boundary check and the phase constraints. First, it is checked whether the belief still needs to be updated considering the goal value and boundary. If not, the method does not add any new nodes. Otherwise, it checks if the belief needs to be decreased or increased. For each of these, the intents are considered which were in the initial method for changing belief values of the agent in the `agentService`. The initial method contains a switch statement and also additional conditions for the increase/decrease/setting of the beliefs. If a belief is set to a value (instead of de- or increased), the "direction" of that value is considered (e.g., setting to 1, here the maximal value, is considered in the part which tries to increase the belief value).

In many cases, there is no intent which can increase or decrease a belief (see overview in "Intents per belief"). Belief `B3` needs special treatment, since it is the average of the *relatedness beliefs* `B4, B5, B6` and `B7`. If it needs to be increased, the algorithm tries to increase those beliefs, and if it needs to be decreased, vice versa. Sometimes there are additional conditions under which a belief is increased or decreased (taken from the initial model). For example, `B7` is only decreased under the user intent `inform_goal_negative` if `B10` is equal to the maximal value (here: 1). If `B10` is not 1, but `B7` needs to be decreased, first only nodes are added which increase `B10`. There are multiple of these special conditions, and they are documented with comments. Sometimes all intents with a specific subject need to be considered. For this, there are hardcoded lists as attributes to the service. In those cases, generally each of those intents is considered in a `for` loop (some restrictions are made, see section "Restrictions and assumptions of the algorithm"). Additionally, intents with an `unknown` subject need to be considered differently than other intents. Specifics on this can be found in section "Specific handling of unknown intents".

In all cases, a node is only added to the graph if it belongs to a valid phase (i.e., did not go a phase back or skipped a phase), and if it satisfies the additional optimal goal constraints (if any), in case it reaches the goal phase. These "optimal goal constraints" currently only play a role for phase 5, in which there are multiple ways to reach the phase, only one of them being optimal (by means of `B2 = 1`). That is, if a node is in phase 5, but `B2 != 1`, then it is not added. For all other phases, the set of these additional constraints is empty.

#### Path finding algorithm

To find the optimal path within the previously generated graph, a simple *Breadth First Search (BFS)* is used. This is sufficient to find the shortest path, since the graph we are working with is a tree. Each node has branches to new nodes with the different intents. There are no backwards connections, or connections between nodes at the same level. The BFS starts from the root of the tree (i.e., the agents state at the beginning of the graph generation). We can traverse backwards from the first encountered node in the next phase over its parents, finding the shortest path to the root. If no such node exists, there is no path to the next phase. As described in the previous section, this is possible due to the restrictions on belief updates. There also may be different same-length optimal paths. This algorithm essentially chooses a random optimal path from them, as it chooses the first node in phase 5, and the edges coming from a node are shuffled in order.

### Some observations on initial model

As will be shown in the overview in the next section, some beliefs which are in the BDI model are not used, and/or cannot be updated (`B9` is not modifiable, `B14` is neither modifiable nor used). Concerning the output of the algorithm, the generated conversation is unnatural, and not a conversation which should or would happen with a real child. Especially from phase 4 to phase 5, the optimal way to increase `B2` to 1 most often involves sending the same user intent repeatedly. An example of a generated "conversation" which also shows this behavior, can be found in section "Example output".

## Intents per belief
In the following, the intents which increase/decrease/set specific beliefs (and additional conditions) are laid out. Here, 0.1 corresponds to the variable `oneStep` and 0.2 to the variable `twoSteps` in the code. Intents with the subject being `unknown` are added here as well, however they require specific implementation and consideration, explained in the section after this one.

- **B1**:
	- *Increase*: `request_goal_effect`, `request_goal_feeling`, `request_goal_howchild`, `request_unknown_feeling` (only if subject is `goal`) => By 0.1
	- *Decrease*: -
	- *Setting*: -
- **B2**:
	- *Increase*: `ack_unknown_compliment` (only if subject is `goal` or `confidant`, is not valid in nlu) => By 0.1
	- *Decrease*: -
	- *Setting*: -
- **B3**:
	- *Increase*: -
	- *Decrease*: -
	- *Setting*: After each intention it is set to the average belief value of `B4`, `B5`, `B6` and `B7`. These need to be influenced to influence `B3`.
- **B4**:
	- *Increase*: `request_chitchat_greeting`, `request_chitchat_faring` => By 0.1
	- *Decrease*: -
	- *Setting*: -
- **B5**:
	- *Increase*: `ack_unknown_empathize` (only if the subject is `bullying` or `goal`)
	  => Also increases if the type is `ack` and attribute is `neutral`, which according to our analysis will never occur however. This would on the one hand be possible with `ack_unknown_neutral`, however since an `unknown` subject will be set to the previous one, this will not result in the correct combination. Also, instead of the type being `ack` from the beginning, it can be `confirm` which is parsed to an `ack` (`confirm_bullying_summary`, `confirm_confidant_summary`, `confirm_goal_summary`, `confirm_confidant_teacher`, `confirm_goal_collaborate`, `confirm_chitchat_satisfaction`, `confirm_confidant_parent`). This also does not lead to the type being type `ack` and attribute `neutral`, since then the attribute is set to `helpful`, `positive` or `negative`, but never `neutral`. Thus, this condition is never actually fulfilled.
	- *Decrease*: -
	- *Setting*: -
- **B6**:
	- *Increase*: If the type is `request` and attribute is `bullying` (currently all intents with subject `bullying` apart from `confirm_bullying_summary`) => By 0.1
	- *Decrease*: -
	- *Setting*: -
- **B7**:
	- *Increase*:
		- `ack_contactingkt_compliment`, `inform_goal_help` => By 0.1
		- `confirm_goal_collaborate` => If B4 > 0.5: By 0.1
	- *Decrease*: `inform_goal_negative`, `inform_unknown_negative` (only if subject is `goal`) => If B10 = 1: By 0.1
	- *Setting*: -
- **B8**:
	- *Increase*:
		- `ack_contactingkt_compliment`, `inform_goal_help` => By 0.1
		- `confirm_goal_collaborate` => If B4 > 0.5: By 0.2
	- *Decrease*: -
	- *Setting*:
		- `inform_goal_negative`, `inform_unknown_negative` (only if the subject is `goal`) => If B10 = 1: To 0
		- `confirm_confidant_teacher` => If B13 > 0.5: To 0
- **B9**: => Not modifiable
- **B10**:
	- *Increase*: -
	- *Decrease*: -
	- *Setting*:
		- If subject is `goal`, `inform_unknown_negative` (only if the current subject is `goal`) => To 1
		- If subject is `bullying` => To 0
- **B11**:
	- *Increase*: `request_goal_effect`, `request_goal_feeling`, `request_goal_howchild`, `request_unknown_feeling` (only if subject is `goal`) => By 0.1
	- *Decrease*: -
	- *Setting*:
		- `request_goal_dream` => To 1
- **B12**:
	- *Increase*: -
	- *Decrease*: -
	- *Setting*:
		- `request_confidant_who`, `request_unknown_who` (only if subject is `confidant`) => To 1
- **B13**:
	- *Increase*: `request_goal_effect`, `request_goal_feeling`, `request_goal_howchild`, `request_unknown_feeling` (only if subject is `goal`) => By 0.1
	- *Decrease*: -
	- *Setting*: `inform_confidant_help`, `inform_confidant_say` => To 1
- **B14**: => Not modifiable, and also not used
- **B15**:
	- *Increase*: -
	- *Decrease*: -
	- *Setting*: `request_chitchat_goodbye` => To 1
- **B16**:
	- *Increase*: -
	- *Decrease*: -
	- *Setting*:
		- `confirm_goal_collaborate` => If B4 > 0.5: To 1
		- `confirm_confidant_teacher` => If B13 > 0.5: To 1
- **B17**:
	- *Increase*: -
	- *Decrease*: -
	- *Setting*: `inform_unknown_positive` (only if subject was `goal`) => If B10 = 1: To 1

### Specific handling of unknown intents

If the user intents subject is `unknown`, it is set to the previous intents' subject. Some intents which influence beliefs can only occur, if the actual user intent was one with an `unknown` subject, and it is set to a specific subject instead (see next section for specific examples). In such cases, there are different approaches in the algorithm.

If it is an intent with a subject which can be set by "itself", i.e., the user intent with that subject exists also in the nlu, then it is only added as a node with the `unknown` subject if the current subject is the one needed for it. To illustrate this, an example follows. `request_unknown_feeling` is an intent which increases `B1`, if its subject is set to `goal`. There is also `request_goal_feeling` in the nlu. `request_goal_feeling` is always added to the considered nodes/edges, `request_unknown_feeling` however only if the current subject is `goal`.

If it is an intent which is not in the nlu, two edges/nodes will be added to the graph if the current subject is not the correct one. The first one being an intent which does not influence the following phases (negatively), but sets the `subject` of the agent to the correct one. For this, usually all different intents which fulfill these conditions are tried, however restrictions apply (see "Restrictions and assumptions of the algorithm"). After such a node/edge is added, the actual intent is considered (with the `unknown` subject) and a corresponding edge/node is added as well. The specific cases and restrictions are described in the following section. They are also documented with comments in the code.

### Intents in code but not in nlu file

There are some intents which are considered in the code, which are however not in the nlu. These are some intents  that we found, there may be more:

- `ack_bullying_empathize`, `ack_goal_empathize` => These can be "created" by means of `ack_unknown_empathize`, and the subject being `bullying` or `goal` respectively.
- `ack_goal_compliment`, `ack_confidant_compliment` => These can be "created" by means of `ack_unknown_compliment`, and the subject being `confidant` or `goal` respectively.
- `inform_goal_positive` => This can be "created" by means of `inform_unknown_positive`, and the subject being `goal`.
- `request_goal_effect` can (according to our observations) neither be created, nor is it an intent in the nlu.

There are also intents which are in the nlu, however are not considered in the code, resulting in no impact on the agents belief values (and transitively on its intention, and desires).

#### Unknown intents not in nlu file

Below an overview of considered intents per `unknown` intent not in the nlu file is given. If changes to the model are made, the implications on this need to be considered.

**Increase B2:** `ack_unknown_compliment` (subject needs to be set to `confidant` or `goal`)
This belief is currently only used in phase 4 to get to `B2 = 1`. There are some restrictions to which intents are considered:
1. `inform_goal_help`, `confirm_goal_collaborate`: This increases `B7`, and would lead to reaching phase 5, but not with the intended ending of `B2 = 1`. Therefore they are excluded.
2. `request_confidant_who`: Increases `B12`, and if `B3 < 0.5`, this would lead to reaching phase 5, but not with the intended ending (`B2 = 1`). Therefore it is excluded if `B3 < 0.5`.

**Increase B5:** `ack_unknown_empathize` (subject needs to be set to `bullying` or `goal`)
This belief is currently only used in phase 1 indirectly by `B3`. The intents which set the subject to "bullying" only increase `B6` and decrease `B10`, which are valid and helpful settings for phase 1. So if the subject is not the correct one, all intents which can be used to set it, are considered.

**Setting B17:** `inform_unknown_positive` (subject needs to be set to `goal`)
This belief is currently not used, so if the subject is not the correct one, all intents which can be used to set it, are considered. If this belief is added to a specific phase, the interactions of these intents with other intents of the phase have to be considered.

## Constraint handling and specification

Constraints for the phase transitions and desires have a specific form. They are generally implemented in such a way that the constraints themselves are easily adjustable (the algorithm in itself however due to its complexity, is not). They are treated as a boolean formula, each atom being a constraint on one belief. Such a constraint can be true or false, just as an atom in a boolean formula. Mostly, the constraints are encoded in *Disjunctive Normal Form (DNF)*, so a disjunction of conjunctions, since every formula is equivalent to one in DNF. In the code, the constraints are encoded in sets of `BeliefConstraints`, or sets of sets of `BeliefConstraints`. The `constraintService` provides functionality to check if a constraint set in *DNF* (or also in *CNF* - Conjunctive Normal Form) is satisfied.

#### Desire constraints

The constraints for a desire are encoded in DNF (a set of sets of `BeliefConstraints`). For example, for D2 this is (B01 < 0.3) || (B17 = 1) || (B3 < 0.5 && B12 = 1) || (B2 = 1), while for other desires there is only one conjunction, this also being DNF.

#### Phase transition constraints

The phase transition constraints have multiple constraint attributes.

- `Map<Set<BeliefConstraint>, BeliefConstraint> goalValues`: If the set of constraints (*conjunction*) is satisfied, then try to reach the mapped to constraint if it is not yet satisfied. E.g., if `B2 < 1` is the only constraint which needs to be satisfied, to try to get `B2 = 1`, then this would be `(B2 < 1) -> (B2 = 1)`. For phase 2, the set of constraints can contain multiple conditions, since there are two desires for the phase.
- `Set<Set<BeliefConstraint>> impossibleToReach`: If these constraints (*DNF*) are satisfied, it is impossible to reach the next phase. These conditions come from the fact that some beliefs are not modifiable in certain directions, and therefore a phase might be impossible to reach.
- `Set<Set<BeliefConstraint>> optimalGoal`: One of the goal constraints (*DNF*) of the phase may be the only one which should be reached. This attribute specifies the *additional* constraints for reaching the next phase optimally. It is only used for phase 5, for specifying that only `B2 = 1` is the desired outcome.

## Restrictions and assumptions of the algorithm

As mentioned in a previous section, generally the algorithm aims to generate an extensive graph of intents which would influence the beliefs in the correct direction. There are some restrictions to the full generation of the graph, mainly due to the generating taking a long time. If `B3 <= 0.32`, the generation of intents (and therefore edges and nodes) to get through the phases by increasing `B3` is very time intensive. This is because `B3` is the average of `B4, B5, B6` and `B7`. Each of these can be increased to increase `B3`. When generating from phase 1 to phase 5, without any additional restrictions, the generation of the fully extensive graph takes an indefinite amount of time (we stopped the generation manually after around 15 minutes, and a graph with  > 600,000 nodes). For some of these intents, they can be influences with all intents with a specific subject, which generally have the same impact on that belief (may have a different impact on other beliefs). The search space is limited by only considering one of these intents, rather than the whole set, if `B3 <= 0.32`. The specific places where these restrictions are made, are marked with comments in the code. Also, the intents for the greeting of the child are only added to the graph, if it is the first node from the current agents state. The graph generation may take a high amount of time, regardless of the additional hardcoded measures. This is due to the search space still being large.

The current algorithm also does not consider so-called "triggers". These are messages that Rasa sends on its own after a random amount of time within a range has passed during which the user has not sent a message. Due to the aspect of randomness, these triggers are entirely excluded from the optimal path. Therefore, if such a trigger is sent, and the values are influenced, the path should be re-generated manually by the user since those changes are not accounted for. Also, actions are not considered in the algorithm. This is due to the time constraints of the project.

Generally, the generation of the graph in `GraphUpdatingService` is highly coupled to the current implementation. This makes it hard to adapt in the future, and to consider the impact of a single change on the entirety of the algorithm. During our project we were not able to find a way to make the algorithm adaptable, since this would have required more analysis and probably additional research. Some ideas on how this might be possible to change can be found in "Future recommendations".

The algorithm cannot reverse the activity of a desire, which may lead to desires blocking other active desires from becoming the intention of the agent. Currently, this is only the case for phase 2 to 3: For example, if the agent is in phase 2 since D3 is active. The goal would be to go to phase 3 by activating D4. However, D4 may already be activated. It would not be the active desire (intention) since D3 is active. As the algorithm cannot reverse the activity of D3, it cannot reach phase 3. At the moment, the algorithm simply stops generating if such a scenario occurs, creating a dead end intentionally.

As previously mentioned, the intents are not considered per phase mentioned in the `nlu` file. They are merely indirectly tied to the phase, by only considering intents which influence the belief values in the needed direction for the phase.

## Considerations when adapting the model

When the model is adapted (meaning for example the `reason` method is changed, a new user intent is added to the model etc.), mainly the `GraphUpdatingService` would need to be adjusted. Overall, considerations  need to be made on how one change can impact the whole phase model, desires, intentions, and influence transitions between phases. It needs to be analysed whether a change would lead to phases being unreachable, or different desires being activated unintentionally. To give an example, a change may have such an impact that the approach with first trying to make a belief value closer to one to fulfill a condition for improving another which is needed for the current phase ("intermediary nodes"), would not be feasible anymore.

Therefore, merely adjusting the `reason` method and the `updateB(X)` method is not sufficient. Each change requires manual consideration and analysis of its impact. It is not possible to concretely describe an impact any change can have, and what would need to be adjusted. This is different per change, and a single generalization cannot be made. Special consideration needs to also be given to intents which have an `unknown` subject, or which are used to set the subject to a specific one when using `unkown` subjects (or one's that are excluded currently). See the previous sections on how this could look for example.

## Future recommendations

In the future, it would be good to implement or change some of the previously mentioned restrictions of the algorithm. For example, implementing actions and finding a way to handle or include triggers. It may also be possible to create an entirely adaptable algorithm. Implementing and analyzing this was not possible within our time-frame. An approach we would propose is refactoring the `reason` method, to take some constraints and conditions per intent, or condition set. Then, if the constraints and conditions are satisfied, some action is executed, which results in belief modifications, or modifications of the edge. If the reason method is adaptable, those constraints can be used in some way to detect if and how a belief can be influenced, and in which directions it is modifiable. This would certainly require additional research, and further analysis of the model and algorithm, may however be possible.

The algorithm currently considers the ordering of messages (i.e., a path with the same messages in a different order is considered a different path), even though a different ordering may have the same impact (additional conditions in the `reason` method would need to be considered). If a way can be found to prevent this, it would increase the efficiency greatly. Some sets of messages ("sub-paths") could be found to be equivalent, and not all different orderings would need to be generated.

## Example output

To illustrate the algorithm, we will give an example output of a generated "conversation", from phase 1 to phase 5.

*Phase 1*
wie pest jou? -> `request_bullying_who`

*Phase 2*
oh wat vervelend -> `ack_unkown_empathize`

*Phase 3*
ik kan wel met je praten -> `inform_goal_help`
hoe gaat het? -> `request_chitchat_faring`
zullen we samen een oplossing vinden? -> `confirm_goal_collaborate`
oh wat vervelend -> `ack_unknown_empathize`
oh wat vervelend -> `ack_unknown_empathize`
zullen we samen een oplossing vinden? -> `confirm_goal_collaborate`
is er iemand anders die je vertrouwt? -> `request_confidant_who`

*Phase 4*
goed idee -> `ack_unknown_compliment`
goed idee -> `ack_unknown_compliment`
goed idee -> `ack_unknown_compliment`
goed idee -> `ack_unknown_compliment`
goed idee -> `ack_unknown_compliment`
goed idee -> `ack_unknown_compliment`
goed idee -> `ack_unknown_compliment`

*Phase 5*