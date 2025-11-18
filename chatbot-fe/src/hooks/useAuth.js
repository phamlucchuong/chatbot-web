import { useState } from 'react';
import { loginWithEmail, register, logout } from '../api/authApi';

export const useLogin = () => {
    const [loading, setLoading] = useState(false);

    const loginUser = async (formData) => {
        try {
            setLoading(true);
            return await loginWithEmail(formData);
        } catch (err) {
            console.log(err.response?.message || 'Đăng ký thất bại');
            return null;
        } finally {
            setLoading(false);
        }
    };

    return { loginUser, loading };
};

export const useRegister = () => {
    const [loading, setLoading] = useState(false);

    const registerUser = async (formData) => {
        try {
            setLoading(true);
            return await register(formData);
        } catch (e) {
            console.log("Lỗi đăng ký:", e.message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    return { registerUser, loading };
};


export const useLogout = () => {
    const logoutUser = async (token) => {
        try {
            return await logout(token);
        } catch (e) {
            console.log("Lỗi đăng xuất:", e.message);
            return null;
        }
    };

    return { logoutUser };
};