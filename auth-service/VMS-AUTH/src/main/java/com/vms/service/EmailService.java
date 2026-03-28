package com.vms.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // ─────────────────────────────────────────────────────────────────────────
    // SHARED LAYOUT HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String wrap(String bodyContent) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>VMS Notification</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f9;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="580" cellpadding="0" cellspacing="0"
                           style="background:#ffffff;border-radius:12px;overflow:hidden;
                                  box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                      <!-- HEADER -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#1e3a5f 0%%,#2d6a9f 100%%);
                                   padding:32px 40px;text-align:center;">
                          <div style="display:inline-block;background:rgba(255,255,255,0.15);
                                      border-radius:10px;padding:8px 18px;margin-bottom:12px;">
                            <span style="color:#ffffff;font-size:22px;font-weight:700;
                                         letter-spacing:2px;">VMS</span>
                          </div>
                          <p style="margin:0;color:rgba(255,255,255,0.75);
                                    font-size:12px;letter-spacing:1px;text-transform:uppercase;">
                            Vendor Management System
                          </p>
                        </td>
                      </tr>

                      <!-- BODY -->
                      <tr>
                        <td style="padding:40px 40px 32px;">
                          %s
                        </td>
                      </tr>

                      <!-- FOOTER -->
                      <tr>
                        <td style="background:#f8fafc;border-top:1px solid #e8ecf0;
                                   padding:20px 40px;text-align:center;">
                          <p style="margin:0;color:#94a3b8;font-size:11px;line-height:1.6;">
                            This is an automated message from the Vendor Management System.<br/>
                            Please do not reply to this email.
                          </p>
                          <p style="margin:8px 0 0;color:#cbd5e1;font-size:10px;">
                            &copy; 2026 VMS &nbsp;&bull;&nbsp; All rights reserved
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(bodyContent);
    }

    private String infoRow(String label, String value) {
        return """
            <tr>
              <td style="padding:10px 16px;border-bottom:1px solid #f1f5f9;">
                <span style="color:#64748b;font-size:12px;font-weight:600;
                             text-transform:uppercase;letter-spacing:0.5px;">%s</span>
              </td>
              <td style="padding:10px 16px;border-bottom:1px solid #f1f5f9;">
                <span style="color:#1e293b;font-size:13px;font-weight:500;">%s</span>
              </td>
            </tr>
            """.formatted(label, value);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML
            mailSender.send(message);
            log.info("Email sent → {} | Subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. OTP EMAIL
    // ─────────────────────────────────────────────────────────────────────────

    public void sendOtpEmail(String toEmail, String otp) {
        String body = """
            <h2 style="margin:0 0 6px;color:#1e293b;font-size:22px;font-weight:700;">
              Password Reset Request
            </h2>
            <p style="margin:0 0 28px;color:#64748b;font-size:14px;line-height:1.6;">
              We received a request to reset your VMS account password.
              Use the verification code below to proceed.
            </p>

            <!-- OTP BOX -->
            <div style="background:linear-gradient(135deg,#eff6ff,#dbeafe);
                        border:2px dashed #93c5fd;border-radius:12px;
                        padding:28px;text-align:center;margin-bottom:28px;">
              <p style="margin:0 0 6px;color:#3b82f6;font-size:11px;font-weight:700;
                         letter-spacing:2px;text-transform:uppercase;">
                Your Verification Code
              </p>
              <p style="margin:0;color:#1e40af;font-size:42px;font-weight:800;
                         letter-spacing:12px;font-family:'Courier New',monospace;">
                %s
              </p>
              <p style="margin:10px 0 0;color:#60a5fa;font-size:12px;">
                &#9201; Valid for <strong>5 minutes</strong>
              </p>
            </div>

            <!-- WARNING -->
            <div style="background:#fefce8;border-left:4px solid #fbbf24;
                        border-radius:6px;padding:14px 16px;margin-bottom:24px;">
              <p style="margin:0;color:#92400e;font-size:12px;line-height:1.6;">
                <strong>&#9888; Security Notice:</strong>
                If you did not request a password reset, please ignore this email.
                Your account remains secure.
              </p>
            </div>

            <p style="margin:0;color:#94a3b8;font-size:12px;text-align:center;">
              Never share this code with anyone, including VMS support staff.
            </p>
            """.formatted(otp);

        send(toEmail, "🔐 Your VMS Password Reset Code", wrap(body));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. VENDOR WELCOME EMAIL
    // ─────────────────────────────────────────────────────────────────────────

    public void sendVendorCredentials(String email, String accessKey) {
        String body = """
            <!-- WELCOME BADGE -->
            <div style="text-align:center;margin-bottom:28px;">
              <div style="display:inline-block;background:#dcfce7;
                          border-radius:50px;padding:8px 20px;">
                <span style="color:#16a34a;font-size:13px;font-weight:700;">
                  ✓ &nbsp;Account Successfully Created
                </span>
              </div>
            </div>

            <h2 style="margin:0 0 6px;color:#1e293b;font-size:22px;font-weight:700;
                       text-align:center;">
              Welcome to VMS
            </h2>
            <p style="margin:0 0 28px;color:#64748b;font-size:14px;
                      line-height:1.6;text-align:center;">
              Your vendor account has been created and is pending approval.<br/>
              Use the credentials below to access the portal.
            </p>

            <!-- CREDENTIALS TABLE -->
            <div style="background:#f8fafc;border:1px solid #e2e8f0;
                        border-radius:10px;overflow:hidden;margin-bottom:28px;">
              <div style="background:#1e3a5f;padding:12px 16px;">
                <p style="margin:0;color:#ffffff;font-size:12px;font-weight:700;
                           letter-spacing:1px;text-transform:uppercase;">
                  &#128273; &nbsp;Your Login Credentials
                </p>
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0">
                %s
                %s
              </table>
            </div>

            <!-- ACTION BUTTON -->
            <div style="text-align:center;margin-bottom:28px;">
              <a href="#"
                 style="display:inline-block;background:linear-gradient(135deg,#1e3a5f,#2d6a9f);
                        color:#ffffff;text-decoration:none;padding:14px 36px;
                        border-radius:8px;font-size:14px;font-weight:600;
                        letter-spacing:0.5px;">
                Log In to VMS Portal →
              </a>
            </div>

            <!-- IMPORTANT NOTICE -->
            <div style="background:#fff7ed;border-left:4px solid #f97316;
                        border-radius:6px;padding:14px 16px;">
              <p style="margin:0;color:#9a3412;font-size:12px;line-height:1.8;">
                <strong>&#128274; Important:</strong><br/>
                &bull; Log in and <strong>change your access key immediately</strong><br/>
                &bull; Your account is under review — you'll be notified once approved<br/>
                &bull; Do not share your credentials with anyone
              </p>
            </div>
            """.formatted(
                infoRow("Email Address", email),
                infoRow("Access Key", "<span style=\"font-family:'Courier New',monospace;" +
                        "background:#f1f5f9;padding:3px 8px;border-radius:4px;" +
                        "color:#dc2626;font-weight:700;\">" + accessKey + "</span>")
            );

        send(email, "🎉 Welcome to VMS — Your Vendor Account is Ready", wrap(body));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. STAFF WELCOME EMAIL
    // ─────────────────────────────────────────────────────────────────────────

    public void sendStaffCredentials(String email, String name, String role, String accessKey) {
        String roleColor = switch (role.toUpperCase()) {
            case "ADMIN"       -> "#7c3aed";
            case "PROCUREMENT" -> "#0369a1";
            case "FINANCE"     -> "#047857";
            default            -> "#1e3a5f";
        };

        String roleIcon = switch (role.toUpperCase()) {
            case "ADMIN"       -> "&#128081;";
            case "PROCUREMENT" -> "&#128203;";
            case "FINANCE"     -> "&#128200;";
            default            -> "&#128100;";
        };

        String body = """
            <!-- ROLE BADGE -->
            <div style="text-align:center;margin-bottom:28px;">
              <div style="display:inline-block;border-radius:50px;padding:8px 20px;
                          background:%s22;border:1px solid %s44;">
                <span style="color:%s;font-size:13px;font-weight:700;">
                  %s &nbsp;%s Account
                </span>
              </div>
            </div>

            <h2 style="margin:0 0 6px;color:#1e293b;font-size:22px;font-weight:700;
                       text-align:center;">
              Hello, %s!
            </h2>
            <p style="margin:0 0 28px;color:#64748b;font-size:14px;
                      line-height:1.6;text-align:center;">
              Your VMS staff account has been set up.<br/>
              You now have access to the portal with <strong>%s</strong> privileges.
            </p>

            <!-- CREDENTIALS TABLE -->
            <div style="background:#f8fafc;border:1px solid #e2e8f0;
                        border-radius:10px;overflow:hidden;margin-bottom:28px;">
              <div style="background:%s;padding:12px 16px;">
                <p style="margin:0;color:#ffffff;font-size:12px;font-weight:700;
                           letter-spacing:1px;text-transform:uppercase;">
                  &#128273; &nbsp;Your Login Credentials
                </p>
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0">
                %s
                %s
                %s
              </table>
            </div>

            <!-- ACTION BUTTON -->
            <div style="text-align:center;margin-bottom:28px;">
              <a href="#"
                 style="display:inline-block;color:#ffffff;text-decoration:none;
                        padding:14px 36px;border-radius:8px;font-size:14px;
                        font-weight:600;letter-spacing:0.5px;background:%s;">
                Access VMS Portal →
              </a>
            </div>

            <!-- SECURITY NOTICE -->
            <div style="background:#fff7ed;border-left:4px solid #f97316;
                        border-radius:6px;padding:14px 16px;">
              <p style="margin:0;color:#9a3412;font-size:12px;line-height:1.8;">
                <strong>&#128274; Security Reminder:</strong><br/>
                &bull; <strong>Change your access key on first login</strong><br/>
                &bull; Never share your credentials with colleagues<br/>
                &bull; Contact your administrator if you did not request this account
              </p>
            </div>
            """.formatted(
                roleColor, roleColor, roleColor,
                roleIcon, role,
                name, role,
                roleColor,
                infoRow("Full Name", name),
                infoRow("Email Address", email),
                infoRow("Access Key",
                    "<span style=\"font-family:'Courier New',monospace;" +
                    "background:#f1f5f9;padding:3px 8px;border-radius:4px;" +
                    "color:#dc2626;font-weight:700;\">" + accessKey + "</span>"),
                roleColor
            );

        send(email, roleIcon + " VMS — Your " + role + " Account Has Been Created", wrap(body));
    }
}