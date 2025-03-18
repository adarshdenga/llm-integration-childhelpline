# An LLM-based Virtual Agent for Training Child Helpline Counsellors
This repository contains four applications. 

1. dktbdiagent - this is the BDI (web) application meant for deployment using Spring Boot.
2. NLU - this is the NLU component meant for deployment using FastAPI.
3. NLG - this is the NLG component meant for deployment using FastAPI.
4. frontend - this contains the React frontend application.

# Instructions

## Getting the repo on the server:
Follow these steps if you are attempting to setup the application on a server.
https://docs.gitlab.com/ee/gitlab-basics/start-using-git.html

Alternatively, you can use personal access tokens to clone the repo.
1. First, create a personal access token at: `https://<your-gitlab-domain>/-/profile/personal_access_tokens`
2. Clone the git repo with `git clone https://<username>:<access-token>@<your-gitlab-domain>/<project-name>.git`

## Setting up Postgres database
### On local machine
1. Install a postgres server on your local machine. You can find instructions for this through a Google search.
2. Create database for BDI application.
```
CREATE DATABASE dktbase;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE "dktbase" TO postgres;
 ```

3. Include the credentials of your database in the BDI application (in application.properties). The BDI application requires this, otherwise the application will crash.
```
spring.datasource.url=jdbc:postgresql://localhost:5432/dktbase
spring.datasource.username=postgres
spring.datasource.password=password
```
You should be able to run your BDI application successfully now.

### On server
1. Configure a server instance.
2. Configure the [server firewall correctly.](#configure-firewall-on-the-server)
3. Install postgres. You can find instructions for this through a Google search.
   - This can be done by using the following commands, but it might differ for each case
   - `apt update` (ensures the server’s local package index is up-to-date)
   - ` apt install postgresql postgresql-contrib` (installs postgresql)
   - `systemctl start postgresql.service` (ensures the postgresql service is started)
   - `service postgresql status` (checks the status of the service, should display as being active)
4. Connect to the server using a postgres client to make sure everything works. I use psql.

   ```psql "host=<host url> port=5432 dbname=postgres user=<user> password=<password> sslmode=require"```

5. Create database for BDI spring application and configure access.

```
CREATE DATABASE dktbase;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE "dktbase" TO postgres;
 ```

6. Include the credentials of your database in the BDI application (in application.properties) in your Github repository.
```
spring.datasource.url=jdbc:postgresql://<server-name>:5432/dktbase
spring.datasource.username=postgres
spring.datasource.password=password
```
You should be able to run your BDI application successfully now. A more secure option is to use Github password manager (or whatever software management tool you're using) to save the credentials instead so your password isn't exposed.

## NLG and NLU
### Ollama Setup
1. Install Ollama from https://ollama.com/download
2. Download an LLM model. In our experiments we used Llama 3.2, but you can download any model from the Ollama model catalog.
3. Run `ollama serve` to start an Ollama REST API endpoint at http://localhost:11434/. Requests can be made to the API endpoint to prompt the model.

Documentation for the above can be found at https://github.com/ollama/ollama.

## Frontend Web Application
### Local
1. Install npm at https://docs.npmjs.com/downloading-and-installing-node-js-and-npm
2. CD to `/dktfrontend/training-system-frontend`
3. Run `npm install`
4. Run `npm run serve`

The web app will be available at `http://localhost:5601`

### On Server
First, ensure that the frontend configuration is correct. 
Navigate to `/dktfrontend/training-system-frontend/src/config.js` and replace localhost with the server address.
1. Install Nginx:
   - `sudo apt-get update`
   - `sudo apt-get install nginx`
2. Change the line that starts with `root` in `/etc/nginx/sites-available/default` to:
   - `root /<path-to-repo>/dktfrontend/training-system-frontend/dist;`
   - This can be achieved with `sudo nano /etc/nginx/sites-available/default` to edit the file
3. Install nodejs and npm with:
   - `sudo apt-get update`
   - `sudo apt-get install nodejs npm`
4. Navigate to `/<path-to-repo>/dktfrontend/training-system-frontend/`
   - `sudo npm install`
   - `sudo npm run build`
5. Start Nginx with: `sudo systemctl start nginx`

The frontend should be available at `http://<your-server-address>`


## Setting up BDI (Spring Boot) application
### Local
1. Open `/dktbdiagent` with an IDE of your choice as a Maven project. We used IntelliJ.
2. Ensure the postgres DB is running (instructions below).
3. Open `/dktbdiagent/src/main/java/com/bdi/agent/AgentApplication.java` and run the main method.

### On Server 
To run the java application on the server, a change must first be made in `src/main/java/com/bdi/agent/api/WebSocketConfig.java`.

The `registerStompEndpoints` method needs to be changed to:
```
@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/session")
                .setAllowedOrigins("http://lilobottest.ewi.tudelft.nl);
    }
```

1. Install Maven with:
   - `sudo apt-get update`
   - `sudo apt-get install maven -y`
2. Install Java 16 with:
   - `sudo apt-get update`
   - `sudo apt install oracle-java16-installer`
If this doesn't work, try with Java 17 instead
3. Navigate to `/dktbdiagent`
4. Build the project with: `sudo mvn clean package`
If this doesn't work, try `sudo mvn clean package -DskipTests`
5. Run the project with `nohup java -jar target/agent-0.0.1-SNAPSHOT.jar &`

# Configure firewall on the server
Each component of the application as their own associated port. 
You must ensure that the required ports are opened to allow for necessary communication between components and also between users and the components.
Below is a list of the components and their respective ports.
If desired the associated ports can be modified by changing the corresponding config files of the component (this is not recommended).
- NLU Application: Port 8000
- NLG Application: Port 8001
- Postgres database: port 5432
- Spring application: port 8080
- React Application: port 5601 if running locally and port 80 if running on the server

To configure the firewall correctly, the ports for the NLG, NLU, spring application and frontend application must be opened.
1. Use `ufw status` or `ufw status verbose` to check the status of the firewall. You can run this command after each step to ensure that it was completed correctly.
2. `ufw show added` displays what rules were added to the firewall so far. You can also use this command after each step to ensure that it was completed correctly.
3. If you connect to the server via SSH, open the port used for it `ufw allow ssh`.
4. Enable the firewall `ufw enable`
5. Open all required ports (if the application configuration has not been changed then the required ports are: 5005, 8080 and 80).
Use `ufw allow <port-number>` to fully open the entered port. 
If allowing full access to the port is not desired, then the previous command can be extended to only allow access from specific locations. However, you must ensure that the required components can still access those ports.

# Resources (From original Master's thesis project)
Links to data related to this thesis. 
- Experiment data (4TU.ResearchData): https://doi.org/10.4121/17371919
- OSF form: Evaluation of a BDI-based virtual agent for training child helpline counsellors - https://osf.io/hkxzc
- Project storage TU Delft: U:\MScProjectDKT (owned by Merijn Bruijnes)
- Thesis report: http://resolver.tudelft.nl/uuid:f04f8f0b-9ab9-4f1c-a19c-43b164d45cce

Here are some handy links I used throughout the thesis. 
- Data analysis markdown file (Willem-Paul):  https://data.4tu.nl/repository/uuid:0cf03876-0f94-4225-b211-c5971d250002
- Data management plan: https://dmponline.tudelft.nl
- Data science research lectures (Willem-Paul): http://yukon.twi.tudelft.nl/weblectures/cs4125.html 
- De Kindertelefoon e-learning: https://www.linkidstudy.nl
- Human research ethics committee (HREC): https://www.tudelft.nl/over-tu-delft/strategie/integriteitsbeleid/human-research-ethics
  - HREC application: https://labservant.tudelft.nl/
  - Template Informed Consent Form: https://www.tudelft.nl/over-tu-delft/strategie/integriteitsbeleid/human-research-ethics/template-informed-consent-form
- Qualtrics TU Delft: https://tudelft.eu.qualtrics.com/
- OSF form: https://osf.io
  - Computer-based intervention for supporting individuals in changing negative thinking patterns: https://osf.io/v6tkq
  - A support system for people with social diabetes distress: https://osf.io/yb6vg
  - Study on effects of a virtual reality exposure with eye-gaze adaptive virtual cognitions: https://osf.io/q58v4
- Rasa: https://rasa.com
- Remote desktop (weblogin) TU Delft: https://weblogin.tudelft.nl/
- Self service portal TU Delft: https://tudelft.topdesk.net
- Transtheoretical model: https://en.wikipedia.org/wiki/Transtheoretical_model
- Virtual human toolkit: https://vhtoolkit.ict.usc.edu
- System Usability Scale: https://www.usability.gov/how-to-and-tools/methods/system-usability-scale.html
  - SUS in Dutch: https://www.usersense.nl/usability-testing/system-usability-scale-sus
