import React from 'react';

export default function TermsPage() {
  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] py-12 px-4 flex flex-col items-center">
      <div className="w-full max-w-[760px] bg-white p-8 md:p-12 rounded-[28px] border border-[#e3e3e8]/60 shadow-[0_10px_40px_rgba(0,0,0,0.02)] text-left">
        
        <button 
          onClick={() => window.history.back()}
          className="mb-8 px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 transition-all text-[#1d1d1f]"
        >
          ← Quay lại
        </button>

        <h1 className="text-3xl font-extrabold tracking-tight text-[#1d1d1f] mb-2">Điều khoản và Điều kiện sử dụng</h1>
        <p className="text-xs text-[#86868b] mb-8 pb-6 border-b border-[#e3e3e8]">Cập nhật lần cuối: ngày 08 tháng 06 năm 2026</p>

        <div className="space-y-6 text-sm text-[#1d1d1f] leading-relaxed">
          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">1. Quy định chung</h2>
            <p className="text-xs text-[#86868b]">
              Chào mừng bạn đến với hệ thống đặt phòng trực tuyến của chúng tôi (Hệ Thống Đặt Phòng Khách Sạn). Bằng việc đăng ký tài khoản, đăng nhập và sử dụng hệ thống này, bạn đã đồng ý tuân thủ toàn bộ các điều khoản và điều kiện được nêu tại đây. Nếu bạn không đồng ý với bất kỳ phần nào của điều khoản này, vui lòng ngừng sử dụng dịch vụ của hệ thống.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">2. Đăng ký & Bảo mật tài khoản</h2>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1">
              <li>Người dùng cần cung cấp đầy đủ và chính xác thông tin cá nhân bắt buộc khi đăng ký (bao gồm Họ tên, Email, Số điện thoại và số CCCD/Hộ chiếu).</li>
              <li>Bạn có trách nhiệm bảo mật mật khẩu của mình và mọi hoạt động diễn ra dưới tài khoản cá nhân.</li>
              <li><strong>⚠️ Quy định khóa bảo mật:</strong> Nhằm bảo vệ tài khoản khỏi truy cập trái phép, hệ thống sẽ tự động khóa tài khoản tạm thời nếu nhập sai mật khẩu liên tiếp quá 5 lần. Để mở khóa tài khoản, vui lòng liên hệ bộ phận hỗ trợ khách hàng.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">3. Quy trình đặt phòng và hủy phòng</h2>
            <ul className="list-disc list-inside text-xs text-[#86868b] space-y-1">
              <li>Mọi yêu cầu đặt phòng phải tuân theo giá phòng hiện hành hiển thị trên hệ thống tại thời điểm đặt phòng.</li>
              <li>Thông tin phòng và tình trạng phòng trống luôn được cập nhật theo thời gian thực.</li>
              <li>Các chính sách về hủy phòng, phụ phí phát sinh hoặc thay đổi ngày ở sẽ được nêu chi tiết tại từng hạng phòng cụ thể trước khi người dùng xác nhận đặt phòng.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">4. Trách nhiệm của người dùng</h2>
            <p className="text-xs text-[#86868b]">
              Người dùng cam kết không sử dụng hệ thống vào bất kỳ mục đích bất hợp pháp nào, không can thiệp làm gián đoạn hệ thống, không giả mạo thông tin cá nhân hay thông tin thẻ thanh toán, và tuân thủ các quy tắc ứng xử của khách sạn trong suốt quá trình lưu trú.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">5. Quyền hạn của Quản trị viên (Admin)</h2>
            <p className="text-xs text-[#86868b]">
              Admin và nhân viên quản trị (Staff) có quyền giám sát hệ thống, khóa tài khoản vi phạm chính sách, từ chối cung cấp dịch vụ nếu phát hiện thông tin giả mạo hoặc hành vi trục lợi hệ thống đặt phòng mà không cần báo trước.
            </p>
          </section>

          <section>
            <h2 className="text-base font-bold text-[#1d1d1f] mb-2">6. Thay đổi điều khoản</h2>
            <p className="text-xs text-[#86868b]">
              Chúng tôi có quyền sửa đổi các điều khoản sử dụng này bất cứ lúc nào để phù hợp với quy định pháp luật và nâng cao chất lượng dịch vụ. Các thay đổi sẽ có hiệu lực ngay khi được đăng tải công khai trên hệ thống.
            </p>
          </section>
        </div>

      </div>
    </div>
  );
}
