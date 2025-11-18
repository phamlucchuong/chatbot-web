
// src/api/authApi.js
// import axiosClient from './axiosClient';

// const authApi = {
// //   register: (data) => axiosClient.post('/users', data),
// //   login: (data) => axiosClient.post('/auth/token', data),
// //   logout: () => axiosClient.post('/auth/logout'),
// };

// export default authApi;


export async function register(formData) {
    const response = await fetch("http://localhost:8080/healthcare/api/users", {
        method: "POST",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify(formData),
    });

    return response.json();
}

export async function loginWithEmail(formData) {
    const response = await fetch("http://localhost:8080/healthcare/api/auth", {
        method: "POST",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify(formData),
    });

    return response.json();
}

export async function requestPasswordReset(formData) {
    const response = await fetch("http://localhost:8080/healthcare/api/auth/token", {
        method: "POST",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify(formData),
    });

    return response.json();
}

export async function logout(token) {
    const response = await fetch("http://localhost:8080/healthcare/api/auth/logout", {
        method: "POST",
        headers: { "Content-type": "application/json",
            authorization: `Bearer ${token}`
        },
    });

    return response.json();
}