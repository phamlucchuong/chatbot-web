import React, { useState } from 'react';
import LoginModal from '../modals/LoginModal';
import PricingModal from '../modals/PricingModal';

export default function InputBox({ content, handleChange, isTyping, handleSearch }) {

  const [isOpenMenu, setIsOpenMenu] = useState(false);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

  const handleClick = () => {
    setIsLoginModalOpen(true);
  }


  return (
    <div className='py-4 px-4'>
      <div className="w-full max-w-3xl mx-auto border border-gray-600 rounded-2xl px-[20px] py-[15px] shadow">

        <textarea value={content} onChange={handleChange} id="input" rows="2" placeholder="Hỏi chatbot"></textarea>

        <div className="textbox--bottom">
          <div className="textbox--bottom-left">

            <div onClick={() => setIsOpenMenu(!isOpenMenu)} id="plus-icon">
              <i className=" fa-solid fa-plus"></i>

              {isOpenMenu &&
                <ul className='w-64 bg-[#262628] shadow-lg rounded-lg p-4 absolute bottom-16 left-4 z-10'>
                    <li onClick={handleClick} className='hover:bg-gray-800'>
                      <i className="fa-regular fa-image"></i>
                      Hình ảnh
                    </li>
                    <li onClick={handleClick}  className='hover:bg-gray-800'>
                      <i className="fa-solid fa-paperclip"></i>
                      Tệp
                    </li>
                </ul>}
            </div>

            <div onClick={handleClick} className="deepSearch">
              <i className="fa-solid fa-magnifying-glass mx-2"></i>
              <span>Deep search</span>
            </div>
          </div>

          <div className="textbox--bottom-right">
            {
              isTyping
                ? <button onClick={handleSearch}>
                  <i className="fa-solid fa-paper-plane"></i>
                </button>
                : <button onClick={handleClick}>
                  <i className="micro-icon icon fa-solid fa-microphone"></i>
                </button>
            }
          </div>

        </div>
      </div>

      <PricingModal isOpen={isLoginModalOpen} onClose={() => setIsLoginModalOpen(false)} />
    </div>

    
  );

}