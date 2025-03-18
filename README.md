# An LLM-based Virtual Agent for Training Child Helpline Counsellors
This repository contains three applications. 

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
### on local machine
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


## Rasa
### Local
#### Installing Rasa
1. Install anaconda locally if you do not already have it: https://docs.anaconda.com/free/anaconda/install/index.html
2. Install python locally if you do not already have it. We used python 3.10: https://www.python.org/downloads/ 
3. Create a new conda environment for rasa in the anaconda terminal.
   - `conda create --name rasa`
   - `conda activate rasa` to activate this environment
4. See Rasa documentation on how to install Rasa on your machine or follow the instructions below: https://rasa.com/docs/rasa/installation/installing-rasa-open-source/
   - `pip3 install -U pip`
   - `pip3 install rasa`
   - `pip3 install rasa[spacy]`
   - `python3 -m spacy download nl_core_news_lg`
   - If these commands do not work, try with `pip` and `python`.

#### Running Rasa
- Essentially, there are two servers you need to run - the Rasa server for the intent recognition and the custom action server that communicates with the BDI application to retreive a response.
- In two separate anaconda terminals, activate the rasa environment with ```conda activate rasa```
- First CD into the ```/dktrasa``` folder
- To run the custom action server, use ```rasa run actions``` 
- Run the Rasa server in the other terminal using ```rasa run -m models --enable-api --cors  "*"```

### On Server
The following instructions describe how to run Rasa on the server.

#### System Specs
Below are the specs of the server that this was run on (with higher system load it is recommended to upgrade this):
  - Intel(R) Xeon(R) Gold 6148 CPU @ 2.40GHz
  - 4 GB System Memory
  - 47 GB Sysem Storage
  - Ubuntu 22.04.1 LTS

Some addresses need to be changed within the project to run the system on the server.
Navigate to `/dktfrontend/training-system-frontend/src/config.js` and change:
```js
const config = {
    agentServer: 'http://localhost:8080',
    agentWsServer: 'ws://localhost:8080/session',
    rasaServer: 'http://localhost:5005'
};
```
to:
```js
const config = {
    agentServer: 'http://{your.server.address}:8080',
    agentWsServer: 'ws://{your.server.address}:8080/session',
    rasaServer: 'http://{your.server.address}:5005'
};
```


1. Connect to server via SSH.
2. Install miniconda on the server by following these instructions: https://docs.conda.io/projects/conda/en/latest/user-guide/install/linux.html
3. Install python with `sudo apt-get install python3.10`
4. Follow the instructions above for installing rasa at [installing rasa](#installing-rasa) from step 3
5. Run rasa by running the commands below in the `/dktrasa` folder:
   - To run the custom action server, use ```nohup rasa run actions &```
   - Run the Rasa server using ```nohup rasa run -m models --enable-api --cors  "*" &```
   - `nohup` ensures that the process does not die when leaving the ssh, and `&` sends the process to the background


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
- Rasa server: port 5005
- Rasa actions server: port 5055
- Postgres database: port 5432
- Spring application: port 8080
- Vue.js frontend application: port 5601 if running locally and port 80 if running on the server

To configure the firewall correctly, the ports for the rasa server, spring application and frontend application must be opened.
1. Use `ufw status` or `ufw status verbose` to check the status of the firewall. You can run this command after each step to ensure that it was completed correctly.
2. `ufw show added` displays what rules were added to the firewall so far. You can also use this command after each step to ensure that it was completed correctly.
3. If you connect to the server via SSH, open the port used for it `ufw allow ssh`.
4. Enable the firewall `ufw enable`
5. Open all required ports (if the application configuration has not been changed then the required ports are: 5005, 8080 and 80).
Use `ufw allow <port-number>` to fully open the entered port. 
If allowing full access to the port is not desired, then the previous command can be extended to only allow access from specific locations. However, you must ensure that the required components can still access those ports.

# Running the project with Docker

The entire application with all components can also be run together with Docker. This will do the setup for you. 

Beware: the application can run much slower using Docker for Windows. 
To improve performance the application can be run directly in Ubuntu. See how to install Ubuntu for windows at https://ubuntu.com/tutorials/install-ubuntu-on-wsl2-on-windows-10#3-download-ubuntu

To be able to use the application with Docker, some addresses in the repo need to be changed.
 - In `dktbdiagent/src/main/resources/application.properties`\
Change first line:\
`spring.datasource.url=jdbc:postgresql://localhost:5432/dktbase`\
to:\
`spring.datasource.url=jdbc:postgresql://db:5432/dktbase`

 - In `dktrasa/endpoints.yml`\
Change:\
`url: "http://localhost:5055/webhook"` \
to:\
`url: "http://action-server:5055/webhook"`

- In `dktrasa/actions/actions.py`\
Change:\
`BDIAGENT_ENDPOINT = "http://localhost:8080/agent/"` \
`REPORT_ENDPOINT = "http://localhost:8080/report/"` \
to:\
`BDIAGENT_ENDPOINT = "http://dktbdiagent:8080/agent/"` \
`REPORT_ENDPOINT = "http://dktbdiagent:8080/report/"` 

1. Install docker desktop at https://docs.docker.com/engine/install/
2. Open command prompt and navigate to the project repo top directory. 
3. Run the command `docker-compose up`. This may take 5-30 minutes to build.


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

  
=======
# LLM-Integration - Childhelpline



## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.ewi.tudelft.nl/in5000/ii/llm-integration-childhelpline.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](https://gitlab.ewi.tudelft.nl/in5000/ii/llm-integration-childhelpline/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
>>>>>>> ccf87c4e611a691980007023fcd0a6cdac02d021
