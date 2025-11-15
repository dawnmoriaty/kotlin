package com.financial.domain.services.impl

import com.financial.domain.services.IEmailService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.slf4j.LoggerFactory

class EmailService(
    private val smtpHost: String,
    private val smtpPort: Int,
    private val smtpUsername: String,
    private val smtpPassword: String,
    private val fromEmail: String,
    private val fromName: String,
    private val frontendUrl: String
) : IEmailService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun sendPasswordResetEmail(toEmail: String, resetToken: String, userName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val resetLink = "$frontendUrl/reset-password?token=$resetToken"
                val htmlContent = buildPasswordResetEmail(userName, resetLink)

                val email = HtmlEmail().apply {
                    hostName = smtpHost
                    setSmtpPort(this@EmailService.smtpPort)
                    setAuthenticator(DefaultAuthenticator(smtpUsername, smtpPassword))
                    isSSLOnConnect = true
                    setFrom(fromEmail, fromName)
                    subject = "Đặt lại mật khẩu - Financial App"
                    setHtmlMsg(htmlContent)
                    addTo(toEmail)
                }

                email.send()
                logger.info("✅ Password reset email sent to: $toEmail")
                true
            } catch (e: Exception) {
                logger.error("❌ Failed to send password reset email to: $toEmail", e)
                false
            }
        }
    }

    override suspend fun sendWelcomeEmail(toEmail: String, userName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val htmlContent = buildWelcomeEmail(userName)

                val email = HtmlEmail().apply {
                    hostName = smtpHost
                    setSmtpPort(this@EmailService.smtpPort)
                    setAuthenticator(DefaultAuthenticator(smtpUsername, smtpPassword))
                    isSSLOnConnect = true
                    setFrom(fromEmail, fromName)
                    subject = "Chào mừng đến với Financial App!"
                    setHtmlMsg(htmlContent)
                    addTo(toEmail)
                }

                email.send()
                logger.info("✅ Welcome email sent to: $toEmail")
                true
            } catch (e: Exception) {
                logger.error("❌ Failed to send welcome email to: $toEmail", e)
                false
            }
        }
    }

    private fun buildPasswordResetEmail(userName: String, resetLink: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 15px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Đặt lại mật khẩu</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>$userName</strong>,</p>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                        <p>Nhấn vào nút bên dưới để đặt lại mật khẩu:</p>
                        <p style="text-align: center;">
                            <a href="$resetLink" class="button">Đặt lại mật khẩu</a>
                        </p>
                        <p><strong>Lưu ý:</strong> Link này chỉ có hiệu lực trong <strong>15 phút</strong>.</p>
                        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                        <hr>
                        <p style="font-size: 12px; color: #666;">
                            Hoặc copy link sau vào trình duyệt:<br>
                            <a href="$resetLink">$resetLink</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Financial App. All rights reserved.</p>
                        <p>Email này được gửi tự động, vui lòng không reply.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildWelcomeEmail(userName: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .footer { text-align: center; margin-top: 20px; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Chào mừng bạn!</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>$userName</strong>,</p>
                        <p>Chào mừng bạn đến với <strong>Financial App</strong>!</p>
                        <p>Bạn đã đăng ký tài khoản thành công. Hãy bắt đầu quản lý tài chính của bạn ngay hôm nay!</p>
                        <p><strong>Tính năng nổi bật:</strong></p>
                        <ul>
                            <li>📊 Theo dõi thu chi chi tiết</li>
                            <li>📈 Thống kê trực quan</li>
                            <li>🎯 Phân loại giao dịch</li>
                            <li>🔒 Bảo mật cao</li>
                        </ul>
                        <p>Chúc bạn có trải nghiệm tuyệt vời!</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Financial App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}

