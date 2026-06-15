import React from 'react';

export default function PrivacyPage() {
  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] py-12 px-4 flex flex-col items-center">
      <div className="w-full max-w-[760px] bg-white p-8 md:p-12 rounded-[28px] border border-[#e3e3e8]/60 shadow-[0_10px_40px_rgba(0,0,0,0.02)] text-left">
        
        <button 
          onClick={() => window.history.back()}
          className="mb-8 px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 transition-all text-[#1d1d1f]"
        >
          ← Back
        </button>

        <h1 className="text-3xl font-extrabold tracking-tight text-[#1d1d1f] mb-2">Privacy & Data Protection Policy</h1>
        <p className="text-xs text-[#86868b] mb-8 pb-6 border-b border-[#e3e3e8]">Last updated: June 08, 2026</p>

        <div className="space-y-6 text-sm text-[#1d1d1f] leading-relaxed">
          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">1. General Commitment to Data Privacy</h2>
            <p className="text-xs text-[#86868b]">
              We are committed to protecting the personal information and private data of our system users. This policy complies with standard data protection regulations to ensure that all personal information is collected and processed safely, securely, and transparently.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">2. Personal Information Collected</h2>
            <p className="text-xs text-[#86868b]">We collect personal information when you voluntarily provide it or through the linked account registration process:</p>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1 mt-2">
              <li><strong>Required Identification Info:</strong> Full name, email address, phone number, and ID/Passport number.</li>
              <li><strong>Linked Authentication (OAuth2):</strong> Verification tokens and email details from Google/Facebook when utilizing quick sign-in features.</li>
              <li><strong>Booking History:</strong> Stay history, check-in/out dates, transaction values, and selected custom room preferences.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">3. Purpose of Data Usage</h2>
            <p className="text-xs text-[#86868b]">The personal data collected is used solely for the following purposes:</p>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1 mt-2">
              <li>Authenticating user identity and managing member accounts.</li>
              <li>Confirming, modifying, and processing room reservations.</li>
              <li>Contacting users to provide booking details or emergency support.</li>
              <li>Complying with legal requirements (e.g., guest check-in declarations).</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">4. Data Storage and Security</h2>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1">
              <li>User passwords are secure-hashed one-way using the BCrypt algorithm (strength strength &ge; 12) before being stored in the database. No one, including administrators, can read the plaintext password.</li>
              <li>The system employs transport-layer security (HTTPS/TLS) and database protection measures to prevent unauthorized data access.</li>
              <li>We do not share, sell, or lease your personal data to third parties unless required by law.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">5. User Rights</h2>
            <p className="text-xs text-[#86868b]">
              Users have the right to view and modify their personal information directly on their Profile page. You also have the right to request account deletion or withdraw consent for personal data processing by contacting our technical support department.
            </p>
          </section>
        </div>

      </div>
    </div>
  );
}
