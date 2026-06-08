import React from 'react';

export default function PrivacyPage() {
  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] py-12 px-4 flex flex-col items-center">
      <div className="w-full max-w-[760px] bg-white p-8 md:p-12 rounded-[28px] border border-[#e3e3e8]/60 shadow-[0_10px_40px_rgba(0,0,0,0.02)] text-left">
        
        <button 
          onClick={() => window.history.back()}
          className="mb-8 px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 transition-all text-[#1d1d1f]"
        >
          ← Quay lại
        </button>

        <h1 className="text-3xl font-extrabold tracking-tight text-[#1d1d1f] mb-2">Chính sách bảo mật & Bảo vệ dữ liệu</h1>
        <p className="text-xs text-[#86868b] mb-8 pb-6 border-b border-[#e3e3e8]">Cập nhật lần cuối: ngày 08 tháng 06 năm 2026</p>

        <div className="space-y-6 text-sm text-[#1d1d1f] leading-relaxed">
          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">1. Cam kết chung về bảo mật thông tin</h2>
            <p className="text-xs text-[#86868b]">
              Chúng tôi cam kết bảo vệ thông tin cá nhân và dữ liệu riêng tư của người dùng hệ thống. Chính sách này tuân thủ các quy định pháp luật hiện hành của Việt Nam (bao gồm Nghị định 13/2023/NĐ-CP về Bảo vệ dữ liệu cá nhân) nhằm đảo bảo mọi thông tin cá nhân đều được thu thập và xử lý an toàn, minh bạch.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">2. Thông tin cá nhân thu thập</h2>
            <p className="text-xs text-[#86868b]">Chúng tôi thu thập các thông tin cá nhân khi bạn tự nguyện cung cấp hoặc thông qua quá trình đăng ký tài khoản liên kết:</p>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1 mt-2">
              <li><strong>Thông tin định danh bắt buộc:</strong> Họ và tên, địa chỉ email, số điện thoại, và số CCCD/Hộ chiếu.</li>
              <li><strong>Thông tin đăng nhập liên kết (OAuth2):</strong> Token xác thực và thông tin email của Google/Facebook khi sử dụng chức năng đăng nhập nhanh.</li>
              <li><strong>Thông tin đặt phòng:</strong> Lịch sử lưu trú, ngày nhận/trả phòng, giá trị giao dịch và sở thích dịch vụ đi kèm.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">3. Mục đích sử dụng dữ liệu</h2>
            <p className="text-xs text-[#86868b]">Dữ liệu cá nhân thu thập được chỉ sử dụng cho các mục đích sau:</p>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1 mt-2">
              <li>Xác thực danh tính người dùng và quản lý tài khoản thành viên.</li>
              <li>Xác nhận, chỉnh sửa và xử lý các giao dịch đặt phòng.</li>
              <li>Liên hệ gửi thông tin đặt phòng hoặc hỗ trợ khẩn cấp.</li>
              <li>Tuân thủ các yêu cầu pháp lý (ví dụ: khai báo thông tin lưu trú của khách du lịch).</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">4. Lưu trữ và Bảo mật thông tin</h2>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1">
              <li>Mật khẩu người dùng được băm bảo mật một chiều bằng thuật toán BCrypt (độ mạnh tối thiểu 12) trước khi lưu vào cơ sở dữ liệu. Không ai, kể cả Admin, có thể đọc được mật khẩu gốc.</li>
              <li>Hệ thống áp dụng các biện pháp bảo mật đường truyền (HTTPS/TLS) và bảo vệ cơ sở dữ liệu để ngăn chặn tin tặc xâm nhập.</li>
              <li>Chúng tôi không chia sẻ, bán, hoặc cho thuê dữ liệu cá nhân của bạn cho bên thứ ba ngoại trừ trường hợp pháp luật yêu cầu.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">5. Quyền của người dùng</h2>
            <p className="text-xs text-[#86868b]">
              Người dùng có quyền xem, chỉnh sửa thông tin cá nhân của mình trực tiếp tại trang hồ sơ tài khoản (Profile). Bạn cũng có quyền yêu cầu xóa bỏ tài khoản hoặc rút lại sự đồng ý cho phép xử lý dữ liệu cá nhân bằng cách gửi yêu cầu tới bộ phận hỗ trợ kỹ thuật của chúng tôi.
            </p>
          </section>
        </div>

      </div>
    </div>
  );
}
