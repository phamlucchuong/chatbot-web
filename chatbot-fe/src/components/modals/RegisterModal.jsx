
import React, { useState } from 'react';
import { useRegister } from '../../hooks/useAuth';


export default function RegisterModal({ isOpen, onClose, onSwitchToLogin }) {
    if (!isOpen) return null;

    // Ngăn chặn việc đóng modal khi click vào nội dung bên trong
    const handleModalContentClick = (e) => {
        e.stopPropagation();
    };

    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        terms: false,
    });

    const [showPassword, setShowPassword] = useState(false);
    const { registerUser, loading } = useRegister();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const response = await registerUser(formData);
        if (response.ok === true) {
            alert('Đăng ký thành công!');
            onClose(); // Đóng modal sau khi đăng ký thành công
            onSwitchToLogin(); // Chuyển sang modal đăng nhập
        } else {
            // Lỗi đã được hook useRegister xử lý và hiển thị qua state `error`
            // Có thể thêm alert ở đây nếu muốn
            alert(`Đăng ký thất bại: ${response.message}`);
        }
    };



    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex justify-center items-center p-4" onClick={onClose}>
            <div
                className="max-w-lg mx-auto bg-white rounded-lg shadow-xl p-8 sm:p-8 relative"
                onClick={handleModalContentClick}
            >
                {/* Close Button */}
                <button
                    className="absolute top-4 right-4 bg-none border-none text-xl text-gray-600 cursor-pointer p-2 rounded-full transition-colors duration-200 hover:bg-black/5 hover:text-gray-900"
                    onClick={onClose}
                >
                    &times;
                </button>

                <h2 className="text-center text-2xl mb-8 text-gray-600 font-normal">Đăng Ký</h2>

                <form onSubmit={handleSubmit}>
                    {/* {error && <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">{error}</div>} */}

                    <div className="flex gap-4 mb-5">
                        <div className="flex-1">
                            <label htmlFor="fullName" className="block mb-1 text-gray-600 text-sm">Họ Tên</label>
                            <input type="text" id="fullName" name="fullName" value={formData.fullName} onChange={handleChange} required disabled={loading} className="text-gray-900 w-full p-2 border border-gray-300 rounded-md text-sm transition-colors duration-300 focus:outline-none focus:border-blue-400 focus:shadow-[0_0_0_2px_rgba(66,153,225,0.1)]" />
                        </div>
                    </div>

                    <div className="mb-3">
                        <label htmlFor="email" className="block mb-1 text-gray-600 text-sm">Email</label>
                        <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} required disabled={loading} placeholder="Sử dụng email có thật để xác thực." className="text-gray-900 w-full p-2 border border-gray-300 rounded-md text-sm transition-colors duration-300 focus:outline-none focus:border-blue-400 focus:shadow-[0_0_0_2px_rgba(66,153,225,0.1)]" />
                    </div>

                    <div className="mb-3">
                        <label htmlFor="password" className="block mb-1 text-gray-600 text-sm">Mật Khẩu</label>
                        <div className="password-group relative">
                            <input type={showPassword ? "text" : "password"} id="password" name="password" value={formData.password} onChange={handleChange} required disabled={loading} placeholder="Từ 6 đến 50 ký tự, 1 chữ hoa, 1 số." className="text-gray-900 w-full p-2 border border-gray-300 rounded-md text-sm transition-colors duration-300 focus:outline-none focus:border-blue-400 focus:shadow-[0_0_0_2px_rgba(66,153,225,0.1)]" />
                            <button type="button" onClick={() => setShowPassword(!showPassword)} disabled={loading} className="password-toggle absolute right-3 top-1/2 -translate-y-1/2 bg-none border-none cursor-pointer text-gray-600 text-base">
                                {showPassword
                                    ? <i className="fa-solid fa-eye-slash text-gray-600"></i>
                                    : <i className="fa-solid fa-eye text-gray-600"></i>
                                }
                            </button>
                        </div>
                    </div>

                    <div className="checkbox-group flex items-start gap-3 my-6">
                        <input type="checkbox" id="terms" name="terms" checked={formData.terms} onChange={handleChange} required disabled={loading} className="mt-1" />
                        <label htmlFor="terms" className="text-xs leading-snug text-gray-600 m-0">
                            Tôi đồng ý với <a href="#" className="text-blue-400 no-underline hover:underline">Thỏa thuận sử dụng</a> và <a href="#" className="text-blue-400 no-underline hover:underline">Quy định bảo mật</a>.
                        </label>
                    </div>

                    <div className="h-[55px] flex gap-4 mb-3">
                        <button type="submit" disabled={loading} className="w-full p-4 bg-[#282A2C] text-white border-none rounded-md text-base font-bold cursor-pointer transition-all duration-300 hover:scale-105 disabled:bg-gray-400 disabled:cursor-not-allowed">
                            {loading ? 'Đang xử lý...' : 'Đăng Ký'}
                        </button>

                    </div>

                    <div className="login-link text-center my-5 text-sm text-gray-700">
                        Đã có tài khoản? <a onClick={onSwitchToLogin} className="text-[#282A2C] no-underline font-bold hover:underline cursor-pointer">Đăng Nhập</a>
                    </div>

                </form>
            </div>
        </div>
    );
}