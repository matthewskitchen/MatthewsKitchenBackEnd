// test-smtp.js
const nodemailer = require('nodemailer');

async function main(){
  const user = process.env.SMTP_USER;
  const pass = process.env.SMTP_PASS;
  const to = process.env.TEST_TO || user;

  if(!user || !pass){
    console.error("❌ SMTP_USER or SMTP_PASS not set");
    return;
  }

  let transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 587,
    secure: false,
    auth: { user, pass },
    logger: true,
    debug: true
  });

  try {
    const info = await transporter.sendMail({
      from: user,
      to: to,
      subject: 'Railway Email Test',
      text: 'If you receive this, SMTP works from Railway!'
    });

    console.log("✅ Email Sent Successfully:", info);
  } catch (err) {
    console.error("❌ Email Send Error:", err);
  }
}

main();

