def send_password_email(to_email: str, generated_password: str):
    # En producción, integrar SMTP / SendGrid / etc.
    print(f"[EMAIL] To: {to_email} | Password: {generated_password}")