/* ============================================================
   SCRIPT CREACIÓN MODELO DDAA
   Base: SQL Server
   Tablas existentes NO modificadas:
   - COMUNA
   - RUTS
   - INSTALACION
   ============================================================ */


/* ============================================================
   1. TABLA CUENCA
   ============================================================ */

CREATE TABLE CUENCA (
    ID_Cuenca INT IDENTITY(1,1) NOT NULL,
    Nombre NVARCHAR(255) NOT NULL,

    CONSTRAINT PK_CUENCA PRIMARY KEY (ID_Cuenca),
    CONSTRAINT UQ_CUENCA_Nombre UNIQUE (Nombre)
);


/* ============================================================
   2. TABLA SUBCUENCA
   ============================================================ */

CREATE TABLE SUBCUENCA (
    ID_Subcuenca INT IDENTITY(1,1) NOT NULL,
    FK_ID_CUENCA INT NOT NULL,

    NombreSubcuenca NVARCHAR(255) NOT NULL,
    SHAC NVARCHAR(255) NULL,
    Reserva NVARCHAR(255) NULL,

    DeclaracionAgotamiento BIT NULL,
    ZonaRestriccionSuperficial BIT NULL,
    ZonaRestriccionSubterranea BIT NULL,

    EstadoSHAC NVARCHAR(255) NULL,
    PlanGestionHidrica BIT NULL,

    CONSTRAINT PK_SUBCUENCA PRIMARY KEY (ID_Subcuenca),

    CONSTRAINT FK_SUBCUENCA_CUENCA
        FOREIGN KEY (FK_ID_CUENCA)
        REFERENCES CUENCA(ID_Cuenca)
);


/* ============================================================
   3. TABLA FUENTE
   ============================================================ */

CREATE TABLE FUENTE (
    ID_Fuente INT IDENTITY(1,1) NOT NULL,
    FK_ID_SUBCUENCA INT NULL,

    Nombre NVARCHAR(255) NOT NULL,
    Tipo NVARCHAR(100) NULL,

    CONSTRAINT PK_FUENTE PRIMARY KEY (ID_Fuente),

    CONSTRAINT FK_FUENTE_SUBCUENCA
        FOREIGN KEY (FK_ID_SUBCUENCA)
        REFERENCES SUBCUENCA(ID_Subcuenca)
);


/* ============================================================
   4. TABLA DDAA
   Tabla principal de Derechos de Aprovechamiento de Aguas
   ============================================================ */

CREATE TABLE DDAA (
    ID_ddaa INT IDENTITY(1,1) NOT NULL,

    FK_ID_COMUNA VARCHAR(50) NOT NULL,
    FK_ID_RUT_TITULAR BIGINT NOT NULL,
    FK_ID_INSTALACION INT NULL,
    FK_ID_FUENTE INT NOT NULL,

    NombreFuenteDerecho NVARCHAR(255) NULL,
    Naturaleza_derecho NVARCHAR(255) NULL,
    Tipo_Derecho NVARCHAR(255) NULL,
    EstadoDerecho NVARCHAR(255) NULL,

    CONSTRAINT PK_DDAA PRIMARY KEY (ID_ddaa),

    CONSTRAINT FK_DDAA_COMUNA
        FOREIGN KEY (FK_ID_COMUNA)
        REFERENCES COMUNA(ID_Comuna),

    CONSTRAINT FK_DDAA_RUT_TITULAR
        FOREIGN KEY (FK_ID_RUT_TITULAR)
        REFERENCES RUTS(Rut),

    CONSTRAINT FK_DDAA_INSTALACION
        FOREIGN KEY (FK_ID_INSTALACION)
        REFERENCES INSTALACION(ID_Instalacion),

    CONSTRAINT FK_DDAA_FUENTE
        FOREIGN KEY (FK_ID_FUENTE)
        REFERENCES FUENTE(ID_Fuente)
);


/* ============================================================
   5. TABLA DDAA_PAGO_NO_USO
   Pago de patente por no uso
   ============================================================ */

CREATE TABLE DDAA_PAGO_NO_USO (
    folio_tgr INT NOT NULL,
    FK_ID_DDAA INT NOT NULL,

    fecha_cobro DATE NULL,
    caudal_aplicado_ls DECIMAL(18,4) NULL,
    factor_aplicado INT NULL,
    patente_utm DECIMAL(18,4) NULL,
    patente_clp DECIMAL(18,2) NULL,

    CONSTRAINT PK_DDAA_PAGO_NO_USO PRIMARY KEY (folio_tgr),

    CONSTRAINT FK_DDAA_PAGO_NO_USO_DDAA
        FOREIGN KEY (FK_ID_DDAA)
        REFERENCES DDAA(ID_ddaa)
);


/* ============================================================
   6. TABLA DDAA_EXPEDIENTE
   Expedientes administrativos asociados a DDAA
   ============================================================ */

CREATE TABLE DDAA_EXPEDIENTE (
    id INT IDENTITY(1,1) NOT NULL,

    codigo NVARCHAR(50) NOT NULL,
    tipo NVARCHAR(50) NOT NULL,
    estado NVARCHAR(50) NOT NULL,

    resolucion_dga_n NVARCHAR(50) NULL,
    resolucion_dga_fecha DATE NULL,
    resolucion_dga_link NVARCHAR(2048) NULL,

    catastro_n INT NULL,
    catastro_fecha DATE NULL,
    catastro_link NVARCHAR(2048) NULL,

    CONSTRAINT PK_DDAA_EXPEDIENTE PRIMARY KEY (id),
    CONSTRAINT UQ_DDAA_EXPEDIENTE_codigo UNIQUE (codigo)
);


/* ============================================================
   7. TABLA PUENTE DDAA_DDAA_EXPEDIENTE
   Relación muchos a muchos entre DDAA y Expedientes
   ============================================================ */

CREATE TABLE DDAA_DDAA_EXPEDIENTE (
    id_ddaa INT NOT NULL,
    id_expediente INT NOT NULL,

    CONSTRAINT PK_DDAA_DDAA_EXPEDIENTE
        PRIMARY KEY (id_ddaa, id_expediente),

    CONSTRAINT FK_DDAA_EXP_REL_DDAA
        FOREIGN KEY (id_ddaa)
        REFERENCES DDAA(ID_ddaa),

    CONSTRAINT FK_DDAA_EXP_REL_EXPEDIENTE
        FOREIGN KEY (id_expediente)
        REFERENCES DDAA_EXPEDIENTE(id)
);


/* ============================================================
   8. TABLA DDAA_INSCRIPCION
   Inscripciones en Conservador de Bienes Raíces
   ============================================================ */

CREATE TABLE DDAA_INSCRIPCION (
    id INT IDENTITY(1,1) NOT NULL,

    fk_id_ddaa_expediente INT NOT NULL,

    cbr NVARCHAR(100) NULL,
    cbr_fojas NVARCHAR(50) NULL,
    cbr_numero INT NULL,
    cbr_fecha DATE NULL,
    cbr_link NVARCHAR(2048) NULL,

    CONSTRAINT PK_DDAA_INSCRIPCION PRIMARY KEY (id),

    CONSTRAINT FK_DDAA_INSCRIPCION_EXPEDIENTE
        FOREIGN KEY (fk_id_ddaa_expediente)
        REFERENCES DDAA_EXPEDIENTE(id)
);


/* ============================================================
   9. TABLA DDAA_PLAZO
   Plazos asociados a medición, registro y transmisión
   ============================================================ */

CREATE TABLE DDAA_PLAZO (
    ID_Plazo INT IDENTITY(1,1) NOT NULL,

    Clase NVARCHAR(30) NOT NULL,

    caudal_ls_min DECIMAL(18,4) NULL,
    caudal_ls_max DECIMAL(18,4) NULL,

    Resol_Extrac_Efect NVARCHAR(50) NULL,
    Link_Resol_Extrac_efect NVARCHAR(2048) NULL,
    Fecha_Resol_Extrac_efect DATE NULL,

    Plazo_Med_Reg NVARCHAR(255) NOT NULL,
    Plazo_Transmision NVARCHAR(255) NOT NULL,

    Fecha_medicion_reg DATE NULL,
    Fecha_transmision DATE NULL,

    CONSTRAINT PK_DDAA_PLAZO PRIMARY KEY (ID_Plazo)
);


/* ============================================================
   10. TABLA DDAA_OBRA
   Obras de captación, medición o transmisión
   ============================================================ */

CREATE TABLE DDAA_OBRA (
    id INT IDENTITY(1,1) NOT NULL,

    FK_ID_RUT_PROVEEDOR BIGINT NULL,
    FK_ID_DDAA_PLAZO INT NULL,

    TipoObra NVARCHAR(50) NULL,
    EstadoObra BIT NULL,
    Fecha_Sol_Obra DATE NULL,

    Carpeta_Solicitud NVARCHAR(255) NULL,
    CoordenadaObra NVARCHAR(50) NULL,

    ResolucionObra NVARCHAR(50) NULL,
    LinkResolucionObra NVARCHAR(2048) NULL,

    Con_Instrumento BIT NULL,
    CodigoObraDGA NVARCHAR(50) NULL,
    LinkQR NVARCHAR(2048) NULL,
    ReportaDGA BIT NULL,

    CONSTRAINT PK_DDAA_OBRA PRIMARY KEY (id),

    CONSTRAINT FK_DDAA_OBRA_RUT_PROVEEDOR
        FOREIGN KEY (FK_ID_RUT_PROVEEDOR)
        REFERENCES RUTS(Rut),

    CONSTRAINT FK_DDAA_OBRA_PLAZO
        FOREIGN KEY (FK_ID_DDAA_PLAZO)
        REFERENCES DDAA_PLAZO(ID_Plazo)
);


/* ============================================================
   11. TABLA DDAA_EJERCICIO
   Ejercicio del derecho
   ============================================================ */

CREATE TABLE DDAA_EJERCICIO (
    ID_Ejercicio INT IDENTITY(1,1) NOT NULL,

    FK_ID_DDAA INT NOT NULL,

    EjercicioDerecho NVARCHAR(255) NULL,
    ContinuidadDerecho NVARCHAR(255) NULL,

    CONSTRAINT PK_DDAA_EJERCICIO PRIMARY KEY (ID_Ejercicio),

    CONSTRAINT FK_DDAA_EJERCICIO_DDAA
        FOREIGN KEY (FK_ID_DDAA)
        REFERENCES DDAA(ID_ddaa)
);


/* ============================================================
   12. TABLA PUENTE DDAA_EJERCICIO_OBRA
   Relación entre ejercicios y obras
   ============================================================ */

CREATE TABLE DDAA_EJERCICIO_OBRA (
    FK_ID_EJERCICIO INT NOT NULL,
    FK_ID_OBRA INT NOT NULL,

    CONSTRAINT PK_DDAA_EJERCICIO_OBRA
        PRIMARY KEY (FK_ID_EJERCICIO, FK_ID_OBRA),

    CONSTRAINT FK_DDAA_EJERCICIO_OBRA_EJERCICIO
        FOREIGN KEY (FK_ID_EJERCICIO)
        REFERENCES DDAA_EJERCICIO(ID_Ejercicio),

    CONSTRAINT FK_DDAA_EJERCICIO_OBRA_OBRA
        FOREIGN KEY (FK_ID_OBRA)
        REFERENCES DDAA_OBRA(id)
);


/* ============================================================
   13. TABLA DDAA_CAUDAL
   Caudal mensual asociado al ejercicio del derecho
   ============================================================ */

CREATE TABLE DDAA_CAUDAL (
    ID_Caudal INT IDENTITY(1,1) NOT NULL,

    FK_ID_EJERCICIO INT NOT NULL,
    Mes INT NOT NULL,
    CaudalMensual DECIMAL(18,4) NULL,

    CONSTRAINT PK_DDAA_CAUDAL PRIMARY KEY (ID_Caudal),

    CONSTRAINT FK_DDAA_CAUDAL_EJERCICIO
        FOREIGN KEY (FK_ID_EJERCICIO)
        REFERENCES DDAA_EJERCICIO(ID_Ejercicio),

    CONSTRAINT CK_DDAA_CAUDAL_MES
        CHECK (Mes BETWEEN 1 AND 12),

    CONSTRAINT UQ_DDAA_CAUDAL_EJERCICIO_MES
        UNIQUE (FK_ID_EJERCICIO, Mes)
);


/* ============================================================
   14. TABLA DDAA_CAUDAL_ECOLOGICO
   Caudal ecológico mensual asociado al ejercicio
   ============================================================ */

CREATE TABLE DDAA_CAUDAL_ECOLOGICO (
    id_caudal_ecologico INT IDENTITY(1,1) NOT NULL,

    FK_ID_EJERCICIO INT NOT NULL,
    mes INT NOT NULL,
    caudalEcologico DECIMAL(18,4) NULL,

    CONSTRAINT PK_DDAA_CAUDAL_ECOLOGICO 
        PRIMARY KEY (id_caudal_ecologico),

    CONSTRAINT FK_DDAA_CAUDAL_ECOLOGICO_EJERCICIO
        FOREIGN KEY (FK_ID_EJERCICIO)
        REFERENCES DDAA_EJERCICIO(ID_Ejercicio),

    CONSTRAINT CK_DDAA_CAUDAL_ECOLOGICO_MES
        CHECK (mes BETWEEN 1 AND 12),

    CONSTRAINT UQ_DDAA_CAUDAL_ECOLOGICO_EJERCICIO_MES
        UNIQUE (FK_ID_EJERCICIO, mes)
);


/* ============================================================
   15. ÍNDICES RECOMENDADOS SOBRE FK
   ============================================================ */

CREATE INDEX IX_SUBCUENCA_FK_ID_CUENCA
ON SUBCUENCA(FK_ID_CUENCA);

CREATE INDEX IX_FUENTE_FK_ID_SUBCUENCA
ON FUENTE(FK_ID_SUBCUENCA);

CREATE INDEX IX_DDAA_FK_ID_COMUNA
ON DDAA(FK_ID_COMUNA);

CREATE INDEX IX_DDAA_FK_ID_RUT_TITULAR
ON DDAA(FK_ID_RUT_TITULAR);

CREATE INDEX IX_DDAA_FK_ID_INSTALACION
ON DDAA(FK_ID_INSTALACION);

CREATE INDEX IX_DDAA_FK_ID_FUENTE
ON DDAA(FK_ID_FUENTE);

CREATE INDEX IX_DDAA_PAGO_NO_USO_FK_ID_DDAA
ON DDAA_PAGO_NO_USO(FK_ID_DDAA);

CREATE INDEX IX_DDAA_DDAA_EXPEDIENTE_ID_EXPEDIENTE
ON DDAA_DDAA_EXPEDIENTE(id_expediente);

CREATE INDEX IX_DDAA_INSCRIPCION_EXPEDIENTE
ON DDAA_INSCRIPCION(fk_id_ddaa_expediente);

CREATE INDEX IX_DDAA_OBRA_FK_ID_RUT_PROVEEDOR
ON DDAA_OBRA(FK_ID_RUT_PROVEEDOR);

CREATE INDEX IX_DDAA_OBRA_FK_ID_DDAA_PLAZO
ON DDAA_OBRA(FK_ID_DDAA_PLAZO);

CREATE INDEX IX_DDAA_EJERCICIO_FK_ID_DDAA
ON DDAA_EJERCICIO(FK_ID_DDAA);

CREATE INDEX IX_DDAA_EJERCICIO_OBRA_FK_ID_OBRA
ON DDAA_EJERCICIO_OBRA(FK_ID_OBRA);

CREATE INDEX IX_DDAA_CAUDAL_FK_ID_EJERCICIO
ON DDAA_CAUDAL(FK_ID_EJERCICIO);

CREATE INDEX IX_DDAA_CAUDAL_ECOLOGICO_FK_ID_EJERCICIO
ON DDAA_CAUDAL_ECOLOGICO(FK_ID_EJERCICIO);