**Authenticate**
----
  Endpoint for authenticating a user.

* **URL**

  http://localhost:8080/auth/authenticate

* **Method:**

  `POST`

*  **URL Params**

   None

* **Data Params**

  `username=[String]`
  `password=[String]`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{ token : “eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpcmVuZSIsImlhdCI6MTY4NTUyMjI5NCwiZXhwIjoxNjg1NjA4Njk0fQ.DpWy82HW9bGCKLTmuR2ONZC8KFMy1Bqxr5_DAmCm8Y4”, username : "user", role: “LEARNER” }`

* **Error Response:**

  * **Code:** 409 CONFLICT <br />
    **Content:** `{ error : "User doesn't exist" }`

  OR

  * **Code:** 409 CONFLICT <br />
    **Content:** `{ error : "Bad credentials" }`

* **Sample Call:**

  ```javascript
    axios.post('http://localhost:8080/auth/authenticate', {
           username: “user”,
           password: “password”
       }).then(response => {
           commit('setStatus', "Logged In")
           commit('setName', response.data.username)
           commit('setRole', response.data.role)
           commit('setToken', response.data.token)
           return Promise.resolve(response);
       }).catch(error => {
           console.warn("Failed to log in: " + error.response.data)
           commit('logInFailure', error.response.data);
           return Promise.reject(error);
       });
  ```

**Register**
----
Endpoint for registering a new account.

* **URL**

  http://localhost:8080/auth/register

* **Method:**

  `POST`

* **URL Params**

   None

* **Data Params**

  `username=[String]`
  `password=[String]`
  `email=[String]`
  `role=[Role]`
  `code=[String]`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{ token : “eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpcmVuZSIsImlhdCI6MTY4NTUyMjI5NCwiZXhwIjoxNjg1NjA4Njk0fQ.DpWy82HW9bGCKLTmuR2ONZC8KFMy1Bqxr5_DAmCm8Y4”, username : "user", role: “LEARNER” }`

* **Error Response:**

  * **Code:** 409 CONFLICT <br />
    **Content:** `{ error : "Username user already exists" }`

  OR

  * **Code:** 409 CONFLICT <br />
    **Content:** `{ error : "A user with the email address email already exists" }`

  OR

  * **Code:** 409 CONFLICT <br />
    **Content:** `{ error : "This code is not valid" }`

* **Sample Call:**

  ```javascript
    axios.post('http://localhost:8080/auth/register', {
           username: “user”,
           password: “password”,
           email: "email",
           role: "LEARNER",
           code: "code"
       }).then(response => {
           commit('setStatus', "Logged In")
           commit('setName', response.data.username)
           commit('setRole', response.data.role)
           commit('setToken', response.data.token)
           return Promise.resolve(response);
       }).catch(error => {
           console.warn("Failed to sign up: " + error.response.data);
           commit('registerFailure', error.response.data);
           return Promise.reject(error);
       });
  ```

**Get Email**
----
Endpoint which gets the email of the user provided.

* **URL**

  http://localhost:8080/user/email

* **Method:**

  `GET`

* **URL Params**

  None

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `email@gmail.com`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "Not found" }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/email" }`

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/user/email', {
        headers: {
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
        this.email = response.data;
      }).catch((error) => console.log(error));
  ```

**Get Role**
----
Endpoint which gets the role of the user provided.

* **URL**

  http://localhost:8080/user/role

* **Method:**

  `GET`

* **URL Params**

  None

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `LEARNER`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "Not found" }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/role" }`

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/user/role', {
        headers: {
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
        this.role = response.data;
      }).catch((error) => console.log(error));
  ```

**Get Trainers**
----
Endpoint which gets the list of trainers assigned to the user provided.

* **URL**

  http://localhost:8080/user/trainers

* **Method:**

  `GET`

* **URL Params**

  None

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{ "first": 1, "second": "trainerName" }`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "Not found" }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/trainers" }`

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/user/trainers', {
        headers: {
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
        this.trainerList.length = 0;
        const trainers = response.data;

        for (let i = 0; i < trainers.length; i++) {
            this.trainerList.push({
                id: trainers[i].first,
                username: trainers[i].second
            });
        }
      }).catch((error) => console.log(error));
  ```

**Assign Trainer**
----
Endpoint which assigns a trainer to the user provided.

* **URL**

  http://localhost:8080/user/assign_trainer

* **Method:**

  `PATCH`

* **URL Params**

  None

* **Data Params**

  `trainerUsername=[String]`

* **Headers**
  * `'Authorization': 'Bearer ' + token`
  * `'Content-Type': 'text/plain'`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `trainerName`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "User 'user' cannot be found." }`

  OR

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "Trainer 'trainerUsername' does not exist." }`

  OR

  * **Code:** 400 BAD REQUEST <br />
    **Headers:**
    * `Warning: "Cannot assign user as their own trainer"`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/assign_trainer" }`

* **Sample Call:**

  ```javascript
    axios.patch('http://localhost:8080/user/assign_trainer', "trainerName", {
        headers: {
          'Content-Type': 'text/plain',
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
         this.$emit('display-alert', {text: "Assigned new trainer", type: "success", duration: 5000});
         this.displayAlert({text: "Assigned new trainer", type: "success", duration: 5000});
         this.fetchTrainers();
         console.log(response)
         return response.data;
      }).catch((error) => {
         console.log(error);
         const requestedUsername = error.config.data;
         const responseList = error.response.data;
         
         for (let i = 0; i < responseList.length; i++) {
           if (responseList[i].username === requestedUsername) {
               if (!responseList[i].userIsTrainer) {
                  this.displayAlert({text: "Cannot assign learner account as a trainer", type: "error", duration: 5000});
                  return;
               }
               break;
           }
         }
        
        this.displayAlert({text: "Failed to assign new trainer", type: "error", duration: 5000});
        this.fetchTrainers();
        return error.response.data;
      });
  ```

**Remove Trainer**
----
Endpoint which assigns a trainer to the user provided.

* **URL**

  http://localhost:8080/user/remove_trainer

* **Method:**

  `PATCH`

* **URL Params**

  None

* **Data Params**

  `trainerUsername=[String]`

* **Headers**
  * `'Authorization': 'Bearer ' + token`
  * `'Content-Type': 'text/plain'`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `trainerName`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "User 'user' cannot be found." }` <br />
    **Headers:**
    * `Warning: "User with provided username does not exist"`

  OR

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "Trainer 'trainerUsername' does not exist." }` <br />
    **Headers:**
    * `Warning: "Trainer with provided username does not exist"`

  OR

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "'trainerUsername' is no longer a trainer." }` <br />
    **Headers:**
    * `Warning: "Trainer is not assigned to user"`

  OR

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "User with username provided does not exist" }` <br />
    **Headers:**
    * `Warning: "User with username provided does not exist"`

  OR

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "List of trainers to assign is null" }` <br />
    **Headers:**
    * `Warning: "List of trainers to assign is null"`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/remove_trainer" }`

* **Sample Call:**

  ```javascript
    axios.patch('http://localhost:8080/user/remove_trainer', "trainerName", {
        headers: {
          'Content-Type': 'text/plain',
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
         this.$emit('display-alert', {text: "Removed trainer successfully", type: "success", duration: 5000});
         this.displayAlert({text: "Removed trainer successfully", type: "success", duration: 5000});
         this.fetchTrainers();
         return response.data;
      }).catch((error) => {
         this.displayAlert({text: "Failed to remove trainer", type: "error", duration: 5000});
         this.$emit('display-alert', {text: "Failed to remove trainer", type: "error", duration: 5000});
         return error.response.data;
      });
  ```

**Update Role**
----
Endpoint which updates the role of the user provided.

* **URL**

  http://localhost:8080/user/update_role

* **Method:**

  `PATCH`

* **URL Params**

  None

* **Data Params**

  `newRole=[Role]`

* **Headers**
  * `'Authorization': 'Bearer ' + token`
  * `'Content-Type': 'application/json'`

* **Success Response:**

  * **Code:** 200 <br />

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/update_role" }`

* **Sample Call:**

  ```javascript
    axios.patch('http://localhost:8080/user/update_role', "TRAINER", {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
         this.displayAlert({text: "Role changed successfully", type: "success", duration: 5000});
         this.$emit('display-alert', {text: "Role changed successfully", type: "success", duration: 5000});
         this.fetchDetails();
         this.emitter.emit("notification-message", "You are now a " + role + ".");
         this.$store.dispatch('auth/changeRole', role)
      }).catch((error) => console.log(error));
  ```

**Update Password**
----
Endpoint which updates the password of the user provided.

* **URL**

  http://localhost:8080/user/update_password

* **Method:**

  `PATCH`

* **URL Params**

  None

* **Data Params**

  `newPassword=[String]`

* **Headers**
  * `'Authorization': 'Bearer ' + token`
  * `'Content-Type': 'text/plain'`

* **Success Response:**

  * **Code:** 200 <br />

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/update_password" }`

* **Sample Call:**

  ```javascript
    axios.patch('http://localhost:8080/user/update_password', "JZ*4ge%gcFHPkD2J", {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
         this.displayAlert({text: "Password changed successfully", type: "success", duration: 8000});
         this.$emit('display-alert', {text: "Password changed successfully", type: "success", duration: 8000});
         this.resetPasswordOptions();
      }).catch((error) => {
         this.displayAlert({text: "Password failed to change", type: "error", duration: 8000});
         console.log(error)
      });
  ```

**Delete Account**
----
Endpoint which deletes the repository entry of the user provided.

* **URL**

  http://localhost:8080/user/delete

* **Method:**

  `DELETE`

* **URL Params**

  None

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />

* **Error Response:**

  * **Code:** 500 INTERNAL SERVER ERROR <br />
  
  OR

  * **Code:** 404 NOT FOUND <br />

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/user/delete" }`

* **Sample Call:**

  ```javascript
    axios.patch('http://localhost:8080/user/delete', {
        headers: {
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
         console.log(response);
         this.$store.replaceState(defaultState);
         this.$router.push('/');
      }).catch((error) => {
         console.log(error)
      });
  ```

**Check Assigned Trainer**
----
Checks if a trainer is assigned to the provided user.

* **URL**

  http://localhost:8080/user/join_request/:learner

* **Method:**

  `GET`

* **URL Params**

  `learner=[String]`

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`
  * `'Content-Type': 'text/plain'`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `'trainerName' is assigned as a trainer for 'learner'.`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "You are not assigned as learner's trainer." }` <br />
  
  OR

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "There is no learner with the name 'learner'." }` <br />

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
                    status : 401,
                    error : "Unauthorized"
                    path : "/user/join_request/learner" }`

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/user/join_request/learner', {
          headers: {
            'Content-Type': 'text/plain',
            'Authorization': 'Bearer ' + this.$store.state.auth.token
          }
        }).then((response) => {
            console.log(response);
            return true;
        }).catch((error) => {
            console.warn(error.response.data)
            this.emitter.emit("notification-message", error.response.data)
            this.$refs.genericPopup.showAlertPopup(error.response.data)
            return false;
        })
  ```

**Get Chat History Metadata by User**
----
Fetches the list of chat history entries. Each entry consists of metadata only. This metadata is used on the 
frontend to display a list of clickable chats. Clicking the chat in the webpage will result in a different 
endpoint being called for the actual messages.

* **URL**

  http://localhost:8080/history/all

* **Method:**

  `GET`

* **URL Params**

  None

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `[{ conversationId : 1, title : "Conversation 1", timestamp : 2023-06-04T10:11:30 }, 
                   { conversationId : 2, title : "Conversation 2", timestamp : 2023-06-04T10:11:45 }]`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
  
  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
                    status : 401,
                    error : "Unauthorized"
                    path : "/history/all" }`

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/history/all', {
          headers: {
            'Authorization': 'Bearer ' + this.token
          }
        })
        .then(response => {
            this.chats = response.data.map(chat => {
                return {
                    id: chat.conversationId,
                    title: chat.title,
                    date: new Date(chat.timestamp)
                };
            });
        })
        .catch(error => {
            console.error(error); 
        });
  ```

**Delete Conversation**
----
Deletes a conversation. Deleting the conversation will also delete the agent, all beliefs and desires 
related to that agent, and the report file of the conversation.

* **URL**

  http://localhost:8080/history/:conversationId/delete

* **Method:**

  `DELETE`

* **URL Params**

  `conversationId=[Long]`

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `Conversation deleted successfully.`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "Conversation does not belong to user." }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/history/1/delete" }`

  OR

  * **Code:** 404 NOT FOUND <br />

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/history/1/delete', {
          headers: {
            'Authorization': 'Bearer ' + this.token
          }
        })
        .then(response => {
            console.log(response.data)
        })
        .catch(error => {
            console.error(error); 
        });
  ```

**Rename Conversation**
----
Renames a conversation. The name of the conversation does not have to be unique, it only serves as a 
human-readable informal "identification" on the frontend.

* **URL**

  http://localhost:8080/history/:conversationId/rename

* **Method:**

  `PUT`

* **URL Params**

  `conversationId=[Long]`

* **Data Params**

  `newName=[String]`

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `Renamed conversation 'name' to 'newName'.`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "Conversation does not belong to user." }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/history/1/rename" }`

  OR

  * **Code:** 404 NOT FOUND <br />

* **Sample Call:**

  ```javascript
    axios.put('http://localhost:8080/history/1/rename', {
          headers: {
            'Authorization': 'Bearer ' + this.token
          },
          params: {
            newName: "newName"
          }
        })
        .then(response => {
            console.log(response.data)
        })
        .catch(error => {
            console.error(error); 
        });
  ```

**Get Full Conversation**
----
Fetches the full conversation, aka the message history, of a conversation.

* **URL**

  http://localhost:8080/history/:conversationId/chat

* **Method:**

  `GET`

* **URL Params**

  `conversationId=[Long]`

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `[{ message : "message", fromUser : true }]`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "Conversation does not belong to user." }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/history/1/chat" }`

  OR

  * **Code:** 404 NOT FOUND <br />

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/history/1/chat', {
          headers: {
            'Authorization': 'Bearer ' + this.token
          }
        })
        .then(response => {
            this.$refs.webChat.setConversation(response.data);
        })
        .catch(error => {
            console.error(error); 
        });
  ```

**Get Transition History**
----
Fetches the full transition history of a conversation.

* **URL**

  http://localhost:8080/history/:conversationId/transitions

* **Method:**

  `GET`

* **URL Params**

  `conversationId=[Long]`

* **Data Params**

  None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `[{ belief : "B1", value : 0.1, msgText : """,
    logIndex : 1, isManualUpdate : true, beliefUpdateType : "INCREASE" }]`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "Conversation does not belong to user." }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/history/1/chat" }`

  OR

  * **Code:** 404 NOT FOUND <br />

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/history/1/transitions', {
          headers: {
            'Authorization': 'Bearer ' + this.token
          }
        })
        .then(response => {
            const pastBeliefUpdates = response.data;
            pastBeliefUpdates.forEach((beliefUpdate) => { this.processReceivedUpdate(beliefUpdate) }
        )
        .catch(error => {
            console.error(error); 
        });
  ```

**Download Report**
----
Creates and sends a downloadable report of the conversation. The information that is included in the report is 
customizable through the RequestParam booleans.

* **URL**

  http://localhost:8080/history/:conversationId/report

* **Method:**

  `GET`

* **URL Params**

  `conversationId=[Long]`

* **Data Params**

  `abbreviations=[boolean]`
  `all_belief_updates=[boolean]`
  `desire_updates=[boolean]`
  `belief_update_causes=[boolean]`
  `belief_values=[boolean]`

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** Report

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `{ error : "Conversation does not belong to user." }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/history/1/report" }`
  

* **Sample Call:**

  ```javascript
        const options = [
            { id: 'abbreviations', label: 'Show belief and desire abbreviations', value: false },
            { id: 'all_belief_updates', label: 'Show all belief updates', value: false },
            { id: 'desire_updates', label: 'Show desire updates', value: true },
            { id: 'belief_update_causes', label: 'Show belief update causes', value: true },
            { id: 'belief_values', label: 'Show numeric values of updated beliefs', value: false },
        ];
        
        const params = options.reduce((query, option) => {
                const paramName = `${option.id}`;
                query[paramName] = option.value;
                return query;
            }, {});
  
        axios({
                method: 'GET',
                url: 'http://localhost:8080/history/:conversationId/report',
                responseType: 'arraybuffer',
                headers: {
                  'Authorization': 'Bearer ' + this.$store.state.auth.token
                },
                params
            })
            .then(response => {
                const blob = new Blob([response.data], { type: 'application/octet-stream' });
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                const filename = this.selectedChat.title.toLowerCase().replace(/\s+/g, '_') + '.docx';
                link.setAttribute('download', filename);
                document.body.appendChild(link);
                link.click();
            })
            .catch(error => {
                console.error('Error downloading file:', error);
            });
  ```

**Add perception**
----
Adds a perception to the agent and returns the chatbot's reply. Causes the agent to reason and update its beliefs. 
The perception is sent from Rasa.

* **URL**

  http://localhost:8080/agent/:userId

* **Method:**

  `POST`

* **URL Params**

  **Required:**

  `userId=[String]`

* **Data Params**

  `type=[String]`
  `subject=[String]`
  `attribute=[String]`
  `text=[String]`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `Response.`

  OR

  * **Code:** 200 <br />
    **Content:** null

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** null

* **Sample Call:**

  ```javascript
    axios.post('http://localhost:8080/agent/18b3c4682e734f13b8f1019027b9bf68', {
            type: "trigger",
            subject: "s",
            attribute: "a",
            text: ""
          }).then(response => {
            console.log(response.data);
          }).catch(error => {
            console.error(error);
          });
  ```

**Get Report**
----
Gets the transcript of the conversation between the user and the agent.

* **URL**

  http://localhost:8080/report/:userId

* **Method:**

  `GET`

* **URL Params**

  **Required:**

  `userId=[String]`

* **Data Params**

  None

* **Success Response:**

  * **Code:** 200 <br />

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** null

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/report/18b3c4682e734f13b8f1019027b9bf68')
           .then(response => {
            console.log(response.data);
          }).catch(error => {
            console.error(error);
          });
  ```

**Get Conversation**
----
Gets the conversation between the user and the agent as a list of MessageModel objects. 
Can be used at any point in the conversation to retrieve the conversation when the trainer joins the session.

* **URL**

  http://localhost:8080/conversation/:sessionId

* **Method:**

  `GET`

* **URL Params**

  **Required:**

  `sessionId=[String]`

* **Data Params**

  None

* **Success Response:**

  * **Code:** 200 <br />

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `Session not found.`

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/conversation/18b3c4682e734f13b8f1019027b9bf68')
           .then(response => {
            console.log(response.data);
          }).catch(error => {
            console.error(error);
          });
  ```

**Get Past Transitions**
----
Gets the past transitions of the agent as a list of BeliefChangeClientModel objects. 
Can be used at any point in the conversation to retrieve the past transitions when the trainer joins the session.

* **URL**

  http://localhost:8080/transitions/:userId

* **Method:**

  `GET`

* **URL Params**

  **Required:**

  `userId=[String]`

* **Data Params**

  None

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `[{ belief : "B1", value : 0.1, msgText : """, 
                    logIndex : 1, isManualUpdate : true, beliefUpdateType : "INCREASE" }]`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** null

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/transitions/18b3c4682e734f13b8f1019027b9bf68')
           .then(response => {
            console.log(response.data);
          }).catch(error => {
            console.error(error);
          });
  ```

**Start Session**
----
Creates a new agent with the given session id if it doesn't already exist. If the new agent was made by a valid 
user of the application, a new Conversation object is created that binds to the agent and the owning user. This 
Conversation represents the chat as well as some metadata that allows users to access their own past chats on 
the frontend.

* **URL**

  http://localhost:8080/create/:sessionId

* **Method:**

  `POST`

* **URL Params**

  **Required:**

  `sessionId=[String]`

* **Data Params**

  **Not required:**

  `username=[String]`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `Session has been created`

* **Error Response:**

  None

* **Sample Call:**

  ```javascript
    axios.post('http://localhost:8080/create/18b3c4682e734f13b8f1019027b9bf68', null, {
            params: { username: "username" }
          }).then(response => {
            console.log(response.data);
          }).catch(error => {
            console.error(error);
          });
  ```

**Change Agent Mode**
----
This endpoint is used to change the agent mode from manual to automatic and vice versa.

* **URL**

  http://localhost:8080/agent/changeMode/:sessionId

* **Method:**

  `POST`

* **URL Params**

  **Required:**

  `sessionId=[String]`

* **Data Params**

  `isTrainerResponding=[boolean]`

* **Headers**
  * `'Authorization': 'Bearer ' + token`
  * `'Content-Type': 'application/json'`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `Agent trainer mode changed to true`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{ error : "Session not found." }`

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/agent/changeMode/18b3c4682e734f13b8f1019027b9bf68" }`

* **Sample Call:**

  ```javascript
    axios.post('http://localhost:8080/agent/changeMode/18b3c4682e734f13b8f1019027b9bf68', JSON.stringify(true), {
            headers: {
              'Content-Type': 'application/json',
              'Authorization': 'Bearer ' + this.$store.state.auth.token
            }
          }).then(response => {
            console.log(response.data);
          }).catch(error => {
            console.error(error);
          });
  ```

**Update Belief**
----
Updates the value of a specific belief for an agent.

* **URL**

  http://localhost:8080/beliefs/update/:conversationId

* **Method:**

  `PUT`

* **URL Params**

  **Required:**

  `conversationId=[String]`

* **Data Params**

  `belief=[String]`
  `value=[float]`

* **Headers**
  * `'Content-Type': 'application/json'`

* **Success Response:**

  * **Code:** 200 <br />

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />

* **Sample Call:**

  ```javascript
    axios.put('http://localhost:8080/beliefs/update/18b3c4682e734f13b8f1019027b9bf68', {
            headers: {
              'Content-Type': 'application/json'
            },
            params: {
              belief: "B1",
              value: 0.1
            } 
          });
  ```

**Get All Beliefs**
----
Fetches all belief values for an agent.

* **URL**

  http://localhost:8080/beliefs/all/:conversationId

* **Method:**

  `GET`

* **URL Params**

  **Required:**

  `conversationId=[String]`

* **Data Params**

  None

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `[{ belief : "B1", value : 0.5 }, { belief : "B2", value : 0.7}, { belief : "B3", value : 0.3}]`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/beliefs/all/18b3c4682e734f13b8f1019027b9bf68')
          .then(response => {
            const beliefs = response.data;
            beliefs.forEach(receivedBelief => {
              this.previousValues[receivedBelief.belief] = receivedBelief.value;
            });
          })
          .catch(error => {
            console.error(`Failed to fetch beliefs for 18b3c4682e734f13b8f1019027b9bf68`, error);
          });
  ```

**Get All Initial Beliefs from CSV**
----
Fetches all initial belief values. These are the beliefs as seen in the beliefs.csv file. This method is used 
to fetch all beliefs for the Training Portal, to ensure beliefs are shown even if the conversation didn't get 
initialized yet on the backend.

* **URL**

  http://localhost:8080/beliefs/all

* **Method:**

  `GET`

* **URL Params**

  None

* **Data Params**

  None

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `[{ belief : "B1", value : 0.5 }, { belief : "B2", value : 0.7}, { belief : "B3", value : 0.3}]`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/beliefs/all')
          .then(response => {
            this.beliefs = response.data;
            this.beliefs.forEach(belief => {
              this.previousValues[belief.id] = belief.value;
            });
          })
          .catch(error => {
            console.error(`Failed to fetch initial beliefs: `, error);
          });
  ```

**Change Agent to Phase**
----
Sets the given agents phase to a specified one, using exemplary belief values to switch to the phase.

* **URL**

  http://localhost:8080/beliefs/phase

* **Method:**

  `PUT`

* **URL Params**

  None

* **Data Params**

  `sessionId=[String]`
  `phase=[Phase]`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `[{ belief : "B1", value : 0.5 }, { belief : "B2", value : 0.7}, { belief : "B3", value : 0.3}]`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />

* **Sample Call:**

  ```javascript
    axios.put('http://localhost:8080/beliefs/phase', {
          sessionId: "18b3c4682e734f13b8f1019027b9bf68",
          phase: "PHASE2"
        })
          .then(response => {
            for (let i = 0; i < response.data.length; i++) {
              this.beliefs.find(item => item.id === response.data[i].belief).value = response.data[i].value;
            }

            this.beliefs.forEach(belief => { this.previousValues[belief.id] = belief.value; });
          })
          .catch(error => {
            console.error('Failed to switch session 18b3c4682e734f13b8f1019027b9bf68 to PHASE2', error);
          });
  ```

**Get Optimal Path**
----
Gets the optimal path from the current state of the conversation following the Five Phase Model.

* **URL**

  http://localhost:8080/optimal-path/:conversationId

* **Method:**

  `GET`

* **URL Params**

  `conversationId=[String]`

* **Data Params**

   None

* **Headers**
  * `'Authorization': 'Bearer ' + token`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{ nodes : [{ beliefs : [], desires : [], phase : "PHASE1", edge : null }] }`

* **Error Response:**

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `There are no sessions with the given id.`

  OR

  * **Code:** 400 BAD REQUEST <br />
    **Content:** `You are not assigned as trainer.`

  OR

  * **Code:** 400 BAD REQUEST <br />

  OR

  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** `{ timestamp : "2023-06-25T12:54:27.308+00:00",
    status : 401,
    error : "Unauthorized"
    path : "/optimal-path/18b3c4682e734f13b8f1019027b9bf68" }`

* **Sample Call:**

  ```javascript
    axios.get('http://localhost:8080/optimal-path/18b3c4682e734f13b8f1019027b9bf68', {
        headers: {
          'Authorization': 'Bearer ' + this.token
        }
      }).then((response) => {
        this.loading = false;
        this.messagePropsList = response.data.nodes;
        if (this.messagePropsList == null || this.messagePropsList.length === 0) {
          this.activePhase = -1;
          this.activeMessageNode = 0;
          this.dataAvailable = false;
          this.error = true;
        } else {
          this.messageNodeClicked(this.messagePropsList[0], 0);
          this.dataAvailable = true;
        }
      }).catch(() => {
        this.activePhase = -1;
        this.activeMessageNode = 0;
        this.loading = false;
        this.dataAvailable = false;
        this.error = true;
      });
  ```

**Forward Message**
----
Forwards a message to everyone involved in some training session.

* **URL**

  http://localhost:8080/session/:sessionId

* **Method:**

  WebSocket subscription

* **URL Params**

  `sessionId=[String]`

* **Data Params**

  `message=[String]`
  `fromUser=[boolean]`

* **Sample Call:**

  ```javascript
    this.stompClient.subscribe('/topic/session/18b3c4682e734f13b8f1019027b9bf68', (message) => {
        const messageData = JSON.parse(message.body);
        for (const i in messageData) {
           this.$refs.webChat.addMessage(messageData[i].message, messageData[i].fromUser, false);
        }
    });
    
    this.stompClient.send('/app/session/18b3c4682e734f13b8f1019027b9bf68', {}, JSON.stringify({"message": "Hello", "fromUser": true}));
  ```

**Trainer Message**
----
Forwards a message to everyone involved in some training session.

* **URL**

  http://localhost:8080/trainer/:sessionId

* **Method:**

  WebSocket subscription

* **URL Params**

  `sessionId=[String]`

* **Data Params**

  `messagesToSend=[List<String>]`

* **Sample Call:**

  ```javascript
    this.stompClient.subscribe('/topic/trainer/18b3c4682e734f13b8f1019027b9bf68', (message) => {
        const messageData = JSON.parse(message.body);
        for (const i in messageData) {
            this.$refs.webChat.addMessage(messageData[i].message, messageData[i].fromUser, true);
        }
    });
    
    this.stompClient.send('/app/trainer/18b3c4682e734f13b8f1019027b9bf68', {}, JSON.stringify(messagesToSend));
  ```

**Request Session Join**
----
Sends a request to join a session to the user with the given username.

* **URL**

  http://localhost:8080/session/join/:username

* **Method:**

  WebSocket subscription

* **URL Params**

  `username=[String]`

* **Data Params**

  `message=[String]`

* **Sample Call:**

  ```javascript
    this.stompClient.subscribe('/topic/session/join/username', (message) => {
        this.$refs.genericPopup.popupText = message.body + " wants to join your session.";
        this.$refs.genericPopup.acceptText = "Accept";
        this.$refs.genericPopup.declineText = "Decline";
        this.$refs.genericPopup.showOptionsPopupWithData(message.body);
        this.emitter.emit("notification-message", message.body + " wants to join your session");
    });
    
    this.stompClient.send('/app/session/join/myUsername', {}, "username");
  ```

**Accept Session Join**
----
Is used to reply to a session join request. This currently prompts the trainer to join the learner session. 
In the future, a session id will be returned to the learner, which can be used to join the session.

* **URL**

  http://localhost:8080/session/accept/:userId

* **Method:**

  WebSocket subscription

* **URL Params**

  `userId=[String]`

* **Data Params**

  `message=[String]`

* **Sample Call:**

  ```javascript
    this.stompClient.subscribe('/topic/session/accept/myUsername', (message) => {
        const messageData = JSON.parse(message.body);
        if (messageData.accepted) {
          this.joinSession(messageData.sessionId, "username");
          this.emitter.emit("notification-message", "username has accepted your join request.");
        } else {
          this.$refs.genericPopup.showAlertPopup("username declined your session request.")
        }
    });
    
    const response = {'accepted': true, 'sessionId': "18b3c4682e734f13b8f1019027b9bf68"};
    this.stompClient.send('/app/session/accept/' + username, {}, JSON.stringify(response));
  ```

**Trainer Assignment Notification**
----
When a learner assigns or removes a trainer, a message is sent to that trainer.

* **URL**

  http://localhost:8080/session/trainer_assign/:username

* **Method:**

  WebSocket subscription

* **URL Params**

  `username=[String]`

* **Data Params**

  `message=[String]`

* **Sample Call:**

  ```javascript
    this.stompClient.subscribe('/topic/session/trainer_assign/myUsername', (message) => {
        this.emitter.emit("notification-message", message.body);
    });
    
    this.stompClient.send('/app/session/trainer_assign/trainerName', {}, "myUsername has added you as a trainer.");
  ```

**Send Phase of Agent**
----
Sends the phase of the agent to the subscription.

* **URL**

  http://localhost:8080/phase/:userId

* **Method:**

  WebSocket subscription

* **URL Params**

  `userId=[String]`

* **Data Params**

  None

* **Sample Call:**

  ```javascript
    this.stompClient.subscribe('/topic/phase/18b3c4682e734f13b8f1019027b9bf68', (message) => {
        const messageData = JSON.parse(message.body);
        if (messageData.phaseFrom !== null) {
            if (messageData.phaseFrom !== messageData.phaseTo) {
                this.lastTransition = {
                  from: phaseEnumToNumber(messageData.phaseFrom),
                  to: phaseEnumToNumber(messageData.phaseTo)
                }
            }
        } else {
            this.lastTransition = null;
        }
        this.phase = phaseEnumToNumber(JSON.parse(message.body).phaseTo);
    });
    
    this.stompClient.send('/app/phase/18b3c4682e734f13b8f1019027b9bf68');
  ```

**Forward Message**
----
Takes the message that was received from the front-end and updates the agent's belief value accordingly. Used 
when a trainer changes a belief value manually.

* **URL**

  http://localhost:8080/update/:sessionId

* **Method:**

  WebSocket subscription

* **URL Params**

  `sessionId=[String]`

* **Data Params**

  `belief=[String]`
  `value=[float]`

* **Sample Call:**

  ```javascript
    this.stompClient.send('/app/update/18b3c4682e734f13b8f1019027b9bf68', {}, { belief: "B1", value: 0.1 });
  ```