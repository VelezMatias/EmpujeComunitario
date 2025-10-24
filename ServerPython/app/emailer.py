"""
Helpers para envío de email.
"""

import os
import ssl
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from app.config import EMAIL_HOST, EMAIL_PORT, EMAIL_USER, EMAIL_PASSWORD, EMAIL_FROM_NAME

EMAIL_DEV_PRINT = os.getenv("EMAIL_DEV_PRINT", "false").lower() in ("1", "true", "yes")


def _send_smtp_message(from_addr: str, to_addr: str, message: MIMEMultipart) -> bool:
    """Intentar enviar mensaje por SMTP. Primero intenta autenticar si hay credenciales,
    si falla o no hay credenciales intenta envío sin autenticación. Devuelve True si enviado.
    """
    context = ssl.create_default_context()

    # Intentar con autenticación si se tienen credenciales
    if EMAIL_USER and EMAIL_PASSWORD:
        try:
            with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server:
                server.starttls(context=context)
                server.login(EMAIL_USER, EMAIL_PASSWORD)
                server.sendmail(from_addr, to_addr, message.as_string())
                print(f"[EMAIL] Enviado (auth) a {to_addr} via {EMAIL_HOST}:{EMAIL_PORT}")
                return True
        except smtplib.SMTPAuthenticationError as e:
            print(f"[EMAIL] Autenticación SMTP fallida: {e}")
        except Exception as e:
            print(f"[EMAIL] Error enviando con auth: {e}")

    # Intento sin autenticación (útil para servidores locales como MailHog)
    try:
        with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server_noauth:
            server_noauth.sendmail(from_addr, to_addr, message.as_string())
            print(f"[EMAIL] Enviado (no-auth) a {to_addr} via {EMAIL_HOST}:{EMAIL_PORT}")
            return True
    except Exception as e:
        print(f"[EMAIL] Error enviando sin auth: {e}")
        return False


def send_password_email(to_email: str, username: str, generated_password: str) -> bool:
    """Enviar email con credenciales al usuario.
    En modo dev (EMAIL_DEV_PRINT) solo imprime en consola y no intenta SMTP.
    """
    subject = "Credenciales de acceso - Empuje Comunitario"
    from_addr = EMAIL_USER or f"no-reply@{os.getenv('HOSTNAME', 'empuje.local')}"

    text = f"Usuario: {username}\nContraseña: {generated_password}\n"
    html = f"<p>Usuario: <strong>{username}</strong></p><p>Contraseña: <strong>{generated_password}</strong></p>"

    if EMAIL_DEV_PRINT:
        print(f"[EMAIL DEV] To: {to_email}\nSubject: {subject}\n{text}")
        return True

    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"] = f"{EMAIL_FROM_NAME} <{from_addr}>"
    msg["To"] = to_email
    msg.attach(MIMEText(text, "plain"))
    msg.attach(MIMEText(html, "html"))

    return _send_smtp_message(from_addr, to_email, msg)


def send_adhesion_notification(to_email: str | None, evento_id: str, org_id_adherente: int, fecha_hora: str | None = None) -> bool:
    """Notificar adhesión por email.
    - Si to_email es None se usará EMAIL_USER como destino (copia)
    - En modo dev imprimimos en consola
    """
    used_recipient = to_email or EMAIL_USER
    from_addr = EMAIL_USER or f"no-reply@{os.getenv('HOSTNAME', 'empuje.local')}"

    subject = f"Nueva adhesión al evento {evento_id}"

    text = f"Nueva adhesión: org_adherente={org_id_adherente} al evento={evento_id} fecha={fecha_hora or 'desconocida'}"
    if not to_email:
        text += "\nNota: el organizador no tiene email; esta notificación es copia enviada al sistema."
    html = f"<div><h2>Nueva adhesión a tu evento</h2><p>La organización <strong>{org_id_adherente}</strong> se adhirió al evento <strong>{evento_id}</strong>.</p><p>Fecha: {fecha_hora or 'desconocida'}</p>{html_extra}</div>"

    if EMAIL_DEV_PRINT:
        print(f"[EMAIL DEV] To: {used_recipient}\nSubject: {subject}\n{text}")
        return True

    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"] = f"{EMAIL_FROM_NAME} <{from_addr}>"
    msg["To"] = used_recipient
    msg.attach(MIMEText(text, "plain"))
    msg.attach(MIMEText(html, "html"))

    return _send_smtp_message(from_addr, used_recipient, msg)

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
            import smtplib
            import ssl
            from email.mime.text import MIMEText
            from email.mime.multipart import MIMEMultipart
            from app.config import EMAIL_HOST, EMAIL_PORT, EMAIL_USER, EMAIL_PASSWORD, EMAIL_FROM_NAME


            def send_password_email(to_email: str, username: str, generated_password: str):
                if not EMAIL_USER or not EMAIL_PASSWORD:
                    print("[EMAIL ERROR] Configuración de email incompleta. Mostrando en consola:")
                    print(f"[EMAIL] To: {to_email} | Username: {username} | Password: {generated_password}")
                    return False

                message = MIMEMultipart("alternative")
                message["Subject"] = "Credenciales de acceso - Empuje Comunitario"
                message["From"] = f"{EMAIL_FROM_NAME} <{EMAIL_USER}>"
                message["To"] = to_email

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
                      <p>Si tenes alguna duda, no dudes en contactarnos!</p>
                    </div>
                  </body>
                </html>
                """

                text_content = f"Usuario: {username}\nContraseña: {generated_password}"
                message.attach(MIMEText(text_content, "plain"))
                message.attach(MIMEText(html_content, "html"))

                context = ssl.create_default_context()
                try:
                    with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server:
                        server.starttls(context=context)
                        server.login(EMAIL_USER, EMAIL_PASSWORD)
                        server.sendmail(EMAIL_USER, to_email, message.as_string())
                        print(f"[EMAIL EXITOSO] Credenciales enviadas exitosamente a {to_email}")
                        return True
                except smtplib.SMTPAuthenticationError as e:
                    print(f"[EMAIL ERROR] Error de autenticación: {e}")
                    print(f"[EMAIL FALLBACK] To: {to_email} | Username: {username} | Password: {generated_password}")
                    return False
                except Exception as e:
                    print(f"[EMAIL ERROR] Error enviando email: {e}")
                    return False


            def send_adhesion_notification(to_email: str | None, evento_id: str, org_id_adherente: int, fecha_hora: str | None = None):
                """Enviar un email simple notificando que una organización se adhirió a un evento.
                Si no existe email del organizador, se envía la notificación al `EMAIL_USER` (cuenta del sistema)
                incluyendo una nota que indica que el organizador no tiene email.
                Si la configuración de email no está completa, se imprimirá en consola (fallback).
                """
                # Si no hay credenciales del sistema, caemos a fallback por consola
                if not EMAIL_HOST:
                    print(f"[EMAIL] Configuración de SMTP incompleta. Mostrar notificación en consola para {to_email or EMAIL_USER}:")
                    print(f"[EMAIL] Adhesión: org_adherente={org_id_adherente} al evento={evento_id} fecha={fecha_hora}")
                    return False

                used_recipient = to_email or EMAIL_USER
                using_fallback = False
                if not to_email:
                    using_fallback = True
                    print(f"[EMAIL] No se encontró email del organizador. Usando {EMAIL_USER} como destinatario alternativo.")

                # construir mensaje
                message = MIMEMultipart("alternative")
                subject_note = " (COPIA - organizador sin email)" if using_fallback else ""
                message["Subject"] = f"Nueva adhesión al evento {evento_id}{subject_note}"
                message["From"] = f"{EMAIL_FROM_NAME} <{EMAIL_USER}>"
                message["To"] = used_recipient

                html_extra = "<p><em>Nota: el organizador no tiene un email registrado; esta notificación es una copia enviada al sistema.</em></p>" if using_fallback else ""
                html_content = f"""
                <html>
                  <body>
                    <div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\"> 
                      <h2>Nueva adhesión a tu evento</h2>
                      <p>La organización <strong>{org_id_adherente}</strong> se adhirió al evento <strong>{evento_id}</strong>.</p>
                      <p>Fecha: {fecha_hora or 'desconocida'}</p>
                      {html_extra}
                      <p>Podés ver las adhesiones y gestionar el evento en el panel de administración.</p>
                    </div>
                  </body>
                </html>
                """

                text_extra = "\nNota: el organizador no tiene un email registrado; esta notificación es una copia enviada al sistema." if using_fallback else ""
                text_content = f"Nueva adhesión: org_adherente={org_id_adherente} al evento={evento_id} fecha={fecha_hora or 'desconocida'}{text_extra}"
                message.attach(MIMEText(text_content, "plain"))
                message.attach(MIMEText(html_content, "html"))

                context = ssl.create_default_context()
                from_addr = EMAIL_USER if EMAIL_USER else 'no-reply@empuje.local'

                # Intentar con autenticación si hay credenciales
                if EMAIL_USER and EMAIL_PASSWORD:
                    try:
                        with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server:
                            server.starttls(context=context)
                            server.login(EMAIL_USER, EMAIL_PASSWORD)
                            server.sendmail(from_addr, used_recipient, message.as_string())
                            print(f"[EMAIL] Notificación de adhesión enviada a {used_recipient} para evento {evento_id}{' (fallback)' if using_fallback else ''}")
                            return True
                    except smtplib.SMTPAuthenticationError as auth_err:
                        print(f"[EMAIL] Autenticación fallida: {auth_err}. Intentando envío sin autenticación (dev)...")
                    except Exception as e:
                        print(f"[EMAIL ERROR] Error enviando con autenticación: {e}")

                # Intentar sin autenticación (útil para MailHog/local SMTP)
                try:
                    with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server_noauth:
                        server_noauth.sendmail(from_addr, used_recipient, message.as_string())
                        print(f"[EMAIL] Notificación de adhesión enviada a {used_recipient} sin autenticación para evento {evento_id}")
                        return True
                except Exception as send_noauth_err:
                    print(f"[EMAIL ERROR] Envío sin autenticación falló: {send_noauth_err}")
                    return False
