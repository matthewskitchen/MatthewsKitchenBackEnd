<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login & Register</title>
    <style>
        /* 🌿 Minimal White Theme */
        body {
            font-family: 'Inter', Arial, sans-serif;
            background-color: #f8f9fa;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .container {
            background: #ffffff;
            padding: 2.5rem;
            border-radius: 12px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
            width: 340px;
            transition: all 0.4s ease-in-out;
        }

        h2 {
            margin-bottom: 1rem;
            color: #333333;
            font-weight: 600;
            letter-spacing: 0.5px;
        }

        input {
            width: 100%;
            padding: 10px 12px;
            margin: 8px 0;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            background-color: #fafafa;
            font-size: 15px;
            transition: 0.3s;
        }

        input:focus {
            border-color: #1976d2;
            background-color: #fff;
            outline: none;
            box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.1);
        }

        button {
            width: 100%;
            padding: 10px;
            margin-top: 10px;
            border: none;
            border-radius: 8px;
            background-color: #1976d2;
            color: white;
            font-size: 15px;
            font-weight: 500;
            cursor: pointer;
            transition: background-color 0.2s;
        }

        button:hover {
            background-color: #1565c0;
        }

        a {
            color: #1976d2;
            text-decoration: none;
        }

        a:hover {
            text-decoration: underline;
        }

        p {
            font-size: 14px;
            color: #555;
            margin-top: 1rem;
        }

        .form-container, .welcome-page {
            display: none;
        }

        .form-container.active, .welcome-page.active {
            display: block;
        }

        #welcome-page h2 {
            color: #333;
            font-weight: 600;
            margin-bottom: 1rem;
        }

        #logout-button {
            background-color: #e53935;
        }

        #logout-button:hover {
            background-color: #c62828;
        }
    </style>
</head>
<body>
    <div id="app" class="container">
        <!-- Login Form -->
        <div id="login-form-container" class="form-container active">
            <h2>Login</h2>
            <form id="login-form">
                <input type="email" id="login-email" placeholder="Email" required>
                <input type="password" id="login-password" placeholder="Password" required>
                <button type="submit">Login</button>
            </form>
            <p>Don’t have an account? <a href="#" id="show-register">Register</a></p>
        </div>

        <!-- Register Form -->
        <div id="register-form-container" class="form-container">
            <h2>Register</h2>
            <form id="register-form">
                <input type="email" id="register-email" placeholder="Email" required>
                <input type="password" id="register-password" placeholder="Create Password" required>
                <input type="password" id="register-confirm-password" placeholder="Confirm Password" required>
                <button type="submit">Register</button>
            </form>
            <p>Already have an account? <a href="#" id="show-login">Login</a></p>
        </div>

        <!-- Welcome Page -->
        <div id="welcome-page" class="welcome-page">
            <h2 id="welcome-message"></h2>
            <button id="logout-button">Logout</button>
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const app = document.getElementById('app');
            const loginFormContainer = document.getElementById('login-form-container');
            const registerFormContainer = document.getElementById('register-form-container');
            const welcomePage = document.getElementById('welcome-page');
            const showRegisterLink = document.getElementById('show-register');
            const showLoginLink = document.getElementById('show-login');
            const loginForm = document.getElementById('login-form');
            const registerForm = document.getElementById('register-form');
            const welcomeMessage = document.getElementById('welcome-message');
            const logoutButton = document.getElementById('logout-button');

            const backendUrl = 'http://localhost:3000';

            function showPage(page) {
                loginFormContainer.classList.remove('active');
                registerFormContainer.classList.remove('active');
                welcomePage.classList.remove('active');

                if (page === 'login') loginFormContainer.classList.add('active');
                if (page === 'register') registerFormContainer.classList.add('active');
                if (page === 'welcome') welcomePage.classList.add('active');
            }

            showRegisterLink.addEventListener('click', e => {
                e.preventDefault();
                showPage('register');
            });

            showLoginLink.addEventListener('click', e => {
                e.preventDefault();
                showPage('login');
            });

            registerForm.addEventListener('submit', async e => {
                e.preventDefault();
                const email = document.getElementById('register-email').value;
                const password = document.getElementById('register-password').value;
                const confirm = document.getElementById('register-confirm-password').value;

                if (password !== confirm) {
                    alert('Passwords do not match!');
                    return;
                }

                const res = await fetch(`${backendUrl}/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (res.ok) {
                    alert('Registered successfully! Please log in.');
                    registerForm.reset();
                    showPage('login');
                } else {
                    alert(await res.text());
                }
            });

            loginForm.addEventListener('submit', async e => {
                e.preventDefault();
                const email = document.getElementById('login-email').value;
                const password = document.getElementById('login-password').value;

                const res = await fetch(`${backendUrl}/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (res.ok) {
                    localStorage.setItem('user', email);
                    const name = email;
                    welcomeMessage.textContent = `Welcome, ${name}! 👋`;
                    showPage('welcome');
                } else {
                    alert(await res.text());
                }
            });

            logoutButton.addEventListener('click', () => {
                localStorage.removeItem('user');
                showPage('login');
            });

            const savedUser = localStorage.getItem('user');
            if (savedUser) {
                const name = savedUser;
                welcomeMessage.textContent = `Welcome back, ${name}! 👋`;
                showPage('welcome');
            } else {
                showPage('login');
            }
        });
    </script>
</body>
</html>
