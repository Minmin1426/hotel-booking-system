import React from 'react';
import { useTranslation } from 'react-i18next';

export default function LanguageSelector() {
  const { i18n } = useTranslation();

  const changeLanguage = (lng) => {
    i18n.changeLanguage(lng);
  };

  // Get current language, defaulting to 'vi'
  const currentLanguage = i18n.language || 'vi';

  return (
    <div className="flex items-center gap-0.5 bg-[#f5f5f7] p-0.5 rounded-full border border-[#e3e3e8]/50 shadow-inner">
      <button
        onClick={() => changeLanguage('vi')}
        title="Tiếng Việt"
        className={`px-2.5 py-1 rounded-full text-[10px] font-bold tracking-wider transition-all duration-200 cursor-pointer ${
          currentLanguage.startsWith('vi')
            ? 'bg-white text-cyan-600 shadow-sm'
            : 'text-[#86868b] hover:text-[#1d1d1f]'
        }`}
      >
        VI
      </button>
      <button
        onClick={() => changeLanguage('en')}
        title="English"
        className={`px-2.5 py-1 rounded-full text-[10px] font-bold tracking-wider transition-all duration-200 cursor-pointer ${
          currentLanguage.startsWith('en')
            ? 'bg-white text-cyan-600 shadow-sm'
            : 'text-[#86868b] hover:text-[#1d1d1f]'
        }`}
      >
        EN
      </button>
    </div>
  );
}
