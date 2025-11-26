import React, { useRef } from 'react';
import { useModalLogic } from '../../hooks/useModalLogic';
import { useLogin } from '../../hooks/useAuth';
import { showToast } from '../../utils/notify';

const NO_OP = () => { };

/**
 * Component LoginModal - Modal Đăng Nhập
 * (Chỉ tập trung vào UI và kết nối logic)
 */
const LoginModal = ({ isOpen, onClose = NO_OP, onSwitchToRegister = NO_OP }) => {
    // Sử dụng Custom Hook để quản lý trạng thái
    const {
        isClosing,
        isForgotPassword,
        showPassword,
        isLoading,
        setIsLoading,
        handleClose,
        showForgotPassword,
        backToLogin,
        togglePassword,
    } = useModalLogic(onClose);

    const loginFormRef = useRef(null);
    const resetEmailRef = useRef(null);
    const { loginUser } = useLogin();

    // Xử lý submit Đăng nhập
    const handleLoginSubmit = async (e) => {
        e.preventDefault();
        // Giả sử bạn lấy email và password từ form ref hoặc state (dùng state tốt hơn)
        // Đây là ví dụ đơn giản:
        const email = loginFormRef.current.email.value;
        const password = loginFormRef.current.password.value;

        setIsLoading(true);
        const response = await loginUser({ email, password });
        if (response) {
            console.log("Đăng nhập thành công:", response);
            localStorage.setItem("token", response.results.token);
            localStorage.setItem("auth", true);
            showToast('Đăng nhập thành công!', 'success');
            handleClose();
        }
    };

    // Xử lý gửi yêu cầu quên mật khẩu
    const handlePasswordReset = async () => {
        // const email = resetEmailRef.current.value;
        // setIsLoading(true);
        // try {
        //     const response = await requestPasswordReset(email);
        //     alert(response.message);
        //     backToLogin(); // Quay lại màn hình đăng nhập sau khi gửi
        // } catch (error) {
        //     alert(`Lỗi: ${error.message}`);
        // } finally {
        //     setIsLoading(false);
        // }
    };

    if (!isOpen) return null;

    // --- Tailwind Classes (Giữ nguyên từ phần trước) ---
    const modalClasses = `login-modal bg-card backdrop-blur-md rounded-xl w-full max-w-md p-8 relative shadow-xl z-[1000] 
                          transition-all duration-300 ${isClosing ? 'scale-95 opacity-0' : 'scale-100 opacity-100'} 
                          sm:max-w-md md:p-8 sm:p-6 sm:m-0 m-4`;
    const inputClasses = `form-input w-full p-3 border text-[var(--text)] border-theme rounded-lg text-sm bg-surface transition-all duration-200 
                          focus:outline-none focus:border-[var(--primary)] focus:bg-surface focus:shadow-[0_0_0_3px_rgba(59,130,246,0.06)]`;
    const btnPrimaryClasses = `btn btn-primary py-3 px-6 rounded-lg text-sm font-medium cursor-pointer transition-all duration-200 border-none text-[var(--primary-contrast)] 
                               bg-[var(--primary)] hover:brightness-95 w-full sm:w-auto ${isLoading ? 'opacity-70 cursor-not-allowed' : ''}`;
    const btnSecondaryClasses = `btn btn-secondary py-3 px-6 rounded-lg text-sm font-medium cursor-pointer transition-all duration-200 
                                 border border-theme bg-surface text-[var(--text)] hover:brightness-95 w-full sm:w-auto ${isLoading ? 'opacity-70 cursor-not-allowed' : ''}`;
    const overlayClasses = `modal-overlay fixed inset-0 bg-black/50 backdrop-blur-sm z-50 transition-opacity duration-300`;
    // ---------------------------------------------------


    return (
        <div className={overlayClasses} onClick={handleClose}>
            <div className="flex justify-center items-center min-h-screen p-4">
                {/* Dùng onMouseDown/onClick để ngăn chặn việc đóng modal khi click vào modal content */}
                <div className={modalClasses} onClick={(e) => e.stopPropagation()}>

                    {/* Close Button */}
                    <button className="close-btn absolute top-4 right-4 bg-none border-none text-xl muted cursor-pointer p-2 rounded-full transition-colors duration-200 hover:bg-black/5 dark:hover:bg-white/5" onClick={handleClose}>
                        &times;
                    </button>

                    <h2 className="text-2xl font-semibold text-[var(--text)] mb-6 text-left leading-tight sm:text-2xl text-xl">Đăng nhập</h2>

                    {/* Form Quên Mật Khẩu (Forgot Password Form) */}
                    <form id="forgotPasswordForm" style={{ display: isForgotPassword ? 'block' : 'none' }}>
                        <div className="form-group mb-5">
                            <label className="form-label block mb-2 text-[var(--text)] text-sm font-medium">Nhập email để đặt lại mật khẩu</label>
                            <input id="resetEmail" type="email" className={inputClasses} required ref={resetEmailRef} disabled={isLoading} />
                        </div>
                        <div className="form-actions flex gap-3 flex-col-reverse sm:flex-row">
                            <button type="button" className={btnSecondaryClasses} onClick={backToLogin} disabled={isLoading}>Quay lại</button>
                            <button type="button" className={btnPrimaryClasses} onClick={handlePasswordReset} disabled={isLoading}>
                                {isLoading ? 'Đang gửi...' : 'Gửi yêu cầu'}
                            </button>
                        </div>
                    </form>

                    {/* Form Đăng Nhập (Login Form) */}
                    <form id="loginForm" onSubmit={handleLoginSubmit} ref={loginFormRef} style={{ display: isForgotPassword ? 'none' : 'block' }}>

                        {/* <div className="social-buttons flex gap-3 mb-6 flex-col sm:flex-row">
                            <button id="btnGoogleLogin" className="social-btn google-btn flex-1 flex items-center justify-center p-3 border border-black/10 rounded-lg bg-white/80 text-gray-700 text-sm font-medium transition-all duration-200 cursor-pointer hover:bg-white hover:border-black/15 hover:-translate-y-0.5 hover:shadow-md" disabled={isLoading}>
                                <i className="fa-brands fa-google mr-2"></i>
                                Google
                            </button>
                        </div>

                        <div className="divider text-center my-6 relative">
                            <span className="bg-[#F2F2F2] px-4 relative z-10 text-[#282A2C] text-sm">hoặc đăng nhập bằng email</span>
                            <div className="absolute inset-x-0 top-1/2 h-px bg-black/10 z-0"></div>
                        </div> */}

                        <div className="form-group mb-5">
                            <label htmlFor="email" className="form-label block mb-2 text-[var(--text)] text-sm font-medium">Email <span className="text-red-500">*</span></label>
                            <input id="email" name="email" type="email" className={inputClasses} required disabled={isLoading} />
                        </div>

                        <div className="form-group mb-5">
                            <label htmlFor="password" className="form-label block mb-2 text-[var(--text)] text-sm font-medium">Mật khẩu <span className="text-red-500">*</span></label>
                            <div className="password-input-wrapper relative">
                                <input name="password" type={showPassword ? "text" : "password"} className={inputClasses} id="password" required disabled={isLoading} />
                                <button type="button" className="password-toggle absolute right-3 top-1/2 -translate-y-1/2 bg-none border-none cursor-pointer text-lg muted p-1 transition-colors duration-200 hover:text-[var(--text)]" onClick={togglePassword} disabled={isLoading}>
                                    {showPassword
                                        ? <i className="fa-solid fa-eye-slash muted"></i>
                                        : <i className="fa-solid fa-eye muted"></i>
                                    }
                                </button>
                            </div>
                            <div className="forgot-password text-right mt-2">
                                <a id="forget-pwd-btn" href="#" className="text-[var(--primary)] text-sm font-medium hover:underline" onClick={showForgotPassword} disabled={isLoading}>Quên mật khẩu?</a>
                            </div>
                        </div>

                        <div className="form-footer flex justify-between items-center mt-8">
                            <div>
                                <span className="muted text-xs">Chưa có tài khoản? </span>
                                <a onClick={onSwitchToRegister} className="signup-link text-[var(--primary)] text-sm font-bold hover:underline cursor-pointer">Đăng Ký</a>
                            </div>

                            <div className="form-actions flex gap-3 flex-col-reverse sm:flex-row">
                                <button type="button" className={btnSecondaryClasses} onClick={handleClose} disabled={isLoading}>Hủy</button>
                                <button id="btnEmailLogin" type="submit" className={btnPrimaryClasses} disabled={isLoading}>
                                    {isLoading ? 'Đang xử lý...' : 'Đăng nhập'}
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default LoginModal;