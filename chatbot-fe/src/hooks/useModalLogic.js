import { useState } from 'react';

/**
 * Custom Hook để quản lý trạng thái hiển thị modal và các form bên trong.
 * @param {function} onClose - Hàm callback khi modal đóng hoàn toàn.
 * @returns {object} Các trạng thái và hàm xử lý.
 */
export const useModalLogic = (onClose) => {
    const [isClosing, setIsClosing] = useState(false);
    const [isForgotPassword, setIsForgotPassword] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false); // Thêm trạng thái loading

    // Xử lý animation và đóng modal
    const handleClose = () => {
        setIsClosing(true);
        setTimeout(() => {
            onClose();
            setIsClosing(false); // Reset trạng thái sau khi đóng
            setIsForgotPassword(false); // Đảm bảo quay về form Login
        }, 300); // Phù hợp với duration-300
    };

    // Chuyển sang form quên mật khẩu
    const showForgotPassword = (e) => {
        e.preventDefault();
        setIsForgotPassword(true);
    };

    // Quay lại form đăng nhập
    const backToLogin = () => {
        setIsForgotPassword(false);
    };

    // Toggle hiển thị mật khẩu
    const togglePassword = () => {
        setShowPassword(prev => !prev);
    };

    return {
        isClosing,
        isForgotPassword,
        showPassword,
        isLoading,
        setIsLoading,
        handleClose,
        showForgotPassword,
        backToLogin,
        togglePassword,
    };
};