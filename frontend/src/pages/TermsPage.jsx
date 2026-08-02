import React from 'react';

export default function TermsPage() {
  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] py-12 px-4 flex flex-col items-center">
      <div className="w-full max-w-[760px] bg-white p-8 md:p-12 rounded-[28px] border border-[#e3e3e8]/60 shadow-[0_10px_40px_rgba(0,0,0,0.02)] text-left">
        
        <button 
          onClick={() => window.history.back()}
          className="mb-8 px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 transition-all text-[#1d1d1f]"
        >
          ← Back
        </button>

        <h1 className="text-3xl font-extrabold tracking-tight text-[#1d1d1f] mb-2">Terms and Conditions of Use</h1>
        <p className="text-xs text-[#86868b] mb-8 pb-6 border-b border-[#e3e3e8]">Last updated: June 08, 2026</p>

        <div className="space-y-6 text-sm text-[#1d1d1f] leading-relaxed">
          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">1. General Provisions</h2>
            <p className="text-xs text-[#86868b]">
              Welcome to our online reservation system (LuxuryStay). By registering an account, logging in, and using this system, you agree to comply with all terms and conditions stated herein. If you do not agree with any part of these terms, please stop using our services.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">2. Registration & Account Security</h2>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1">
              <li>Users must provide complete and accurate required personal information when registering (including Full Name, Email, Phone Number, and ID/Passport number).</li>
              <li>You are responsible for maintaining the confidentiality of your password and for all activities that occur under your personal account.</li>
              <li><strong>⚠️ Security Lock Rule:</strong> To protect accounts from unauthorized access, the system will automatically lock the account temporarily if the password is typed incorrectly more than 5 times consecutively. To unlock your account, please contact customer support.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">3. Booking and Cancellation Process</h2>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1">
              <li>All booking requests must comply with the current room rates displayed on the system at the time of booking.</li>
              <li>Room details and vacancy status are always updated in real-time.</li>
              <li>Policies regarding cancellations, additional charges, or changes to check-in/out dates will be detailed for each specific room category before the user confirms the booking.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">4. User Responsibilities</h2>
            <p className="text-xs text-[#86868b]">
              Users commit not to use the system for any illegal purposes, not to interfere with or disrupt the system, not to forge personal details or payment info, and to comply with hotel code of conduct rules during their stay.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">5. Administrator (Admin) Authority</h2>
            <p className="text-xs text-[#86868b]">
              Administrators (Admins) and management staff have the right to monitor the system, lock accounts that violate policies, and refuse services if they detect forged details or abusive booking behaviors without prior notice.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">6. Changes to Terms</h2>
            <p className="text-xs text-[#86868b]">
              We reserve the right to modify these terms of use at any time to align with legal regulations and improve service quality. Changes will take effect immediately upon being publicly posted on the system.
            </p>
          </section>
        </div>

      </div>
    </div>
  );
}
