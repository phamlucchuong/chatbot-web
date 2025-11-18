


// File: PricingModal.jsx

import React, { useEffect, useState } from 'react';

const PricingModal = ({ isOpen, onClose }) => {
    const [isVisible, setIsVisible] = useState(false);
    const [isAnimating, setIsAnimating] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setIsVisible(true);
            setTimeout(() => setIsAnimating(true), 10);
        } else {
            setIsAnimating(false);
            setTimeout(() => setIsVisible(false), 300);
        }
    }, [isOpen]);

    if (!isVisible) return null;

    const handleBackdropClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    const pricingOptions = [
        {
            name: 'Free',
            price: 'Miễn phí',
            features: ['Truy vấn không giới hạn', 'Phân tích nâng cao', 'Lưu trữ lịch sử 1 năm', 'Không được bấm cái nút đó'],
            buttonColor: 'bg-gray-500 hover:bg-gray-600',
        },
        {
            name: 'Pro (Đề xuất)',
            price: '99 $/tháng',
            features: ['Truy vấn không giới hạn', 'Phân tích nâng cao', 'Lưu trữ lịch sử 1 năm'],
            buttonColor: 'bg-blue-600 hover:bg-blue-700',
            highlight: true,
        },
        {
            name: 'Premium',
            price: '999 $/tháng',
            features: ['Gói Pro nhưng màu tím'],
            buttonColor: 'bg-purple-600 hover:bg-purple-700',
        },
    ];
    
    const handleClick = (optionName) => {
        if (optionName === 'Free') {
            onClose();
        }
    }

    return (
        <div
            onClick={handleBackdropClick}
            className={`fixed inset-0 z-50 flex items-center justify-center bg-black transition-all duration-300 ease-out
                ${isAnimating ? 'bg-opacity-70' : 'bg-opacity-0'}`}
        >
            <div className={`bg-transparent rounded-lg shadow-2xl p-6 w-full max-w-5xl mx-4 transform transition-all duration-300 ease-out
                ${isAnimating ? 'scale-100 opacity-100 translate-y-0' : 'scale-95 opacity-0 -translate-y-4'}`}>

                {/* Các Thẻ Tùy Chọn Giá */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-6">
                    {pricingOptions.map((option) => (
                        <div
                            key={option.name}
                            className={`relative flex flex-col items-center h-[450px] p-6 rounded-xl border-2 
                                bg-gray bg-opacity-30
                                ${option.highlight
                                ? 'border-blue-600 shadow-xl scale-105'
                                : 'border-gray-200'
                                } transition-transform duration-300 ease-in-out`}
                        >
                            {option.highlight && (
                                <span className="bg-blue-600 text-white text-xs font-semibold px-3 py-1 rounded-full absolute -mt-10">
                                    PHỔ BIẾN
                                </span>
                            )}
                            <h3 className="text-2xl text-white font-bold mb-2 text-center">
                                {option.name}
                            </h3>
                            <p className="text-4xl text-white font-extrabold text-center mb-6">
                                {option.price}
                            </p>

                            {/* Danh sách Tính năng */}
                            <div className="space-y-3 mb-8 text-left">
                                {option.features.map((feature, index) => (
                                    <div key={index} className="flex items-center text-white">
                                        {index <= 2
                                            ? <svg className="w-5 h-5 text-green-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path></svg>
                                            : <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path
                                                    d="M6 18L18 6M6 6L18 18"
                                                    stroke="red"
                                                    stroke-width="2.5"
                                                    stroke-linecap="round"
                                                    stroke-linejoin="round"
                                                />
                                            </svg>
                                        }
                                        {feature}
                                    </div>
                                ))}
                            </div>

                            {/* Nút Chọn */}
                            <button
                                onClick={() => handleClick(option.name)}
                                className={`absolute bottom-8 w-[250px] py-3 rounded-lg text-white font-semibold transition-colors duration-200 
                                    ${option.buttonColor}
                                    ${option.name !== 'Free' ? 'cursor-not-allowed opacity-70' : 'cursor-pointer'}
                                    `}
                            >
                                {option.name === 'Free' ? 'Gói hiện tại' : 'Đăng ký ngay'}
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default PricingModal;