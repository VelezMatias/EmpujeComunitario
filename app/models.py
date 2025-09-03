from sqlalchemy import Column, Integer, String, Boolean, Enum, DateTime, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
from app.db import Base
import enum

class RoleEnum(str, enum.Enum):
    PRESIDENTE = "PRESIDENTE"
    VOCAL = "VOCAL"
    COORDINADOR = "COORDINADOR"
    VOLUNTARIO = "VOLUNTARIO"

class CategoryEnum(str, enum.Enum):
    ROPA = "ROPA"
    ALIMENTOS = "ALIMENTOS"
    JUGUETES = "JUGUETES"
    UTILES_ESCOLARES = "UTILES_ESCOLARES"

def utcnow():
    return datetime.now(timezone.utc)

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True)
    username = Column(String, unique=True, nullable=False, index=True)
    nombre = Column(String, nullable=False)
    apellido = Column(String, nullable=False)
    telefono = Column(String)
    email = Column(String, unique=True, nullable=False, index=True)
    rol = Column(Enum(RoleEnum), nullable=False)
    activo = Column(Boolean, default=True)
    password_hash = Column(String, nullable=False)

    # relaciones
    events = relationship("EventMember", back_populates="user", cascade="all, delete-orphan")

class DonationItem(Base):
    __tablename__ = "donations"
    id = Column(Integer, primary_key=True)
    categoria = Column(Enum(CategoryEnum), nullable=False)
    descripcion = Column(String, nullable=False)
    cantidad = Column(Integer, nullable=False, default=0)
    eliminado = Column(Boolean, default=False)

    created_at = Column(DateTime(timezone=True), default=utcnow, nullable=False)
    created_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    updated_at = Column(DateTime(timezone=True), default=utcnow, onupdate=utcnow, nullable=False)
    updated_by = Column(Integer, ForeignKey("users.id"), nullable=True)

class Event(Base):
    __tablename__ = "events"
    id = Column(Integer, primary_key=True)
    nombre = Column(String, nullable=False)
    descripcion = Column(String, nullable=False)
    fecha_hora = Column(DateTime(timezone=True), nullable=False)

    miembros = relationship("EventMember", back_populates="event", cascade="all, delete-orphan")
    distribuciones = relationship("EventDonationDistribution", back_populates="event", cascade="all, delete-orphan")

class EventMember(Base):
    __tablename__ = "event_members"
    event_id = Column(Integer, ForeignKey("events.id"), primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id"), primary_key=True)
    event = relationship("Event", back_populates="miembros")
    user = relationship("User", back_populates="events")

    __table_args__ = (
        UniqueConstraint('event_id', 'user_id', name='uq_event_user'),
    )

class EventDonationDistribution(Base):
    __tablename__ = "event_dists"
    id = Column(Integer, primary_key=True)
    event_id = Column(Integer, ForeignKey("events.id"), nullable=False)
    donation_item_id = Column(Integer, ForeignKey("donations.id"), nullable=False)
    cantidad = Column(Integer, nullable=False)

    event = relationship("Event", back_populates="distribuciones")