import smtplib
import ssl
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from app.config import EMAIL_HOST, EMAIL_PORT, EMAIL_USER, EMAIL_PASSWORD, EMAIL_FROM_NAME


def send_password_email(to_email: str, username: str, generated_password: str):
    if not EMAIL_USER or not EMAIL_PASSWORD:
        print(f"[EMAIL ERROR] Configuración de email incompleta. Mostrando en consola:")
        print(f"[EMAIL] To: {to_email} | Username: {username} | Password: {generated_password}")
        return False
    
    try:
        message = MIMEMultipart("alternative")
        message["Subject"] = "Credenciales de acceso - Empuje Comunitario"
        message["From"] = f"{EMAIL_FROM_NAME} <{EMAIL_USER}>"
        message["To"] = to_email
        
        # Contenido HTML del email
        html_content = f"""
        <html>
          <body>
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <h2 style="color: #28a745;">¡Bienvenido/a a Empuje Comunitario!</h2>
              
              <p>Se ha creado tu cuenta en nuestro sistema. A continuación encontrarás tus credenciales de acceso:</p>
              
              <div style="background-color: #f8f9fa; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0;">
                <p><strong>Usuario:</strong> {username}</p>
                <p><strong>Contraseña:</strong> {generated_password}</p>
              </div>
              
              <p><strong>Instrucciones de acceso</strong></p>
              <ul>
                <li>Podes ingresar usando tu nombre de usuario</li>
              </ul>
              
              <p>Si tenes alguna duda, no dudes en contactarnos!</p>
              
              <hr style="border: 1px solid #e9ecef; margin: 30px 0;">
            </div>
          </body>
        </html>
        """
        
        text_content = f"""
        ¡Bienvenido/a a Empuje Comunitario!
        
        Se ha creado tu cuenta en nuestro sistema. A continuación encontrarás tus credenciales de acceso:
        
        Usuario: {username}
        Contraseña: {generated_password}
        
        Instrucciones de acceso:
        - Podes ingresar usando tu nombre de usuario
        
        Si tenes alguna duda, no dudes en contactarnos!
        """
        
        part_text = MIMEText(text_content, "plain")
        part_html = MIMEText(html_content, "html")
        
        message.attach(part_text)
        message.attach(part_html)
        
        # Contexto que sea SSL seguro
        context = ssl.create_default_context()
        
        print(f"[EMAIL] Intentando conectar a {EMAIL_HOST}:{EMAIL_PORT}...")
        
        try:
            # Conexión estándar con STARTTLS
            with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server:
                print(f"[EMAIL] Conectado a {EMAIL_HOST}. Iniciando STARTTLS...")
                server.starttls(context=context) 
                print(f"[EMAIL] STARTTLS exitoso. Autenticando...")
                server.login(EMAIL_USER, EMAIL_PASSWORD)
                print(f"[EMAIL] Autenticación exitosa. Enviando mensaje...")
                server.sendmail(EMAIL_USER, to_email, message.as_string())
                print(f"[EMAIL EXITOSO] Credenciales enviadas exitosamente a {to_email}")
                return True
                
        except (smtplib.SMTPConnectError, ConnectionRefusedError, OSError) as e:
            print(f"[EMAIL] Fallo conexión estándar: {e}")
            print(f"[EMAIL] Intentando método alternativo con SSL directo...")
            
            # Conexión SSL directa
            try:
                with smtplib.SMTP_SSL(EMAIL_HOST, 465, timeout=30) as server:
                    print(f"[EMAIL] Conectado con SSL directo. Autenticando...")
                    server.login(EMAIL_USER, EMAIL_PASSWORD)
                    print(f"[EMAIL] Autenticación exitosa. Enviando mensaje...")
                    server.sendmail(EMAIL_USER, to_email, message.as_string())
                    print(f"[EMAIL EXITOSO] Credenciales enviadas exitosamente a {to_email} (SSL directo)")
                    return True
            except Exception as ssl_error:
                print(f"[EMAIL] Fallo también con SSL directo: {ssl_error}")
                raise e
        
    except smtplib.SMTPAuthenticationError as e:
        print(f"[EMAIL ERROR] Error de autenticación: {e}")
        print(f"[EMAIL FALLBACK] To: {to_email} | Username: {username} | Password: {generated_password}")
        return False
        
    except (smtplib.SMTPConnectError, ConnectionRefusedError, OSError) as e:
        print(f"[EMAIL ERROR] Error de conexión de red: {e}")
        print(f"  - Servidor {EMAIL_HOST} no disponible")
        print(f"  - Puerto {EMAIL_PORT} bloqueado")
        print(f"[EMAIL FALLBACK] To: {to_email} | Username: {username} | Password: {generated_password}")
        return False
        
    except smtplib.SMTPException as e:
        print(f"[EMAIL ERROR] Error SMTP general: {e}")
        print(f"[EMAIL FALLBACK] To: {to_email} | Username: {username} | Password: {generated_password}")
        return False
        
    except Exception as e:
        print(f"[EMAIL ERROR] Error inesperado: {e}")
        print(f"[EMAIL FALLBACK] To: {to_email} | Username: {username} | Password: {generated_password}")
        return False