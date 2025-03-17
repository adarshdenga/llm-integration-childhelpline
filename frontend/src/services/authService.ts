import axios from 'axios';

export const authService = {
    async login(username: string, password: string) {
        axios.post('http://lilobottest.ewi.tudelft.nl/bdi/auth/authenticate', {
            username: username,
            password: password
        }).then(response => {
            localStorage.setItem('status', "Logged In")
            localStorage.setItem('username', response.data.username)
            localStorage.setItem('token', response.data.token)
            return Promise.resolve(response);
        }).catch(error => {
            console.warn("Failed to log in: " + error.response.data)
            return Promise.reject(error);
        });
    },

    async register(username: string, password: string, email: string, code: string) {
        axios.post('http://lilobottest.ewi.tudelft.nl/bdi/auth/register', {
            username: username,
            password: password,
            email: email,
            role: "LEARNER",
            code: code
    }).then(response => {
            localStorage.setItem('status', "Logged In")
            localStorage.setItem('username', response.data.username)
            localStorage.setItem('token', response.data.token)
            return Promise.resolve(response);
        }).catch(error => {
            console.warn("Failed to sign up: " + error.response.data);
            return Promise.reject(error);
        });
    },

    logout() {
        localStorage.removeItem('username');
        localStorage.removeItem('bdi_session_id');
    }
};
