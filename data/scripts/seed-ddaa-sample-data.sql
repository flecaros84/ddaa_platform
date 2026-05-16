/*
   Ejecutar en SQL Server sobre la base ddaa.
   Carga datos de ejemplo para probar el CRUD DDAA desde el frontend.
   Es idempotente: se puede ejecutar varias veces sin duplicar los registros principales.
*/

USE [ddaa];

IF NOT EXISTS (SELECT 1 FROM COMUNA WHERE ID_Comuna = N'10101')
    INSERT INTO COMUNA (ID_Comuna, Nombre) VALUES (N'10101', N'Puerto Montt');

IF NOT EXISTS (SELECT 1 FROM COMUNA WHERE ID_Comuna = N'10102')
    INSERT INTO COMUNA (ID_Comuna, Nombre) VALUES (N'10102', N'Calbuco');

IF NOT EXISTS (SELECT 1 FROM COMUNA WHERE ID_Comuna = N'10201')
    INSERT INTO COMUNA (ID_Comuna, Nombre) VALUES (N'10201', N'Castro');

IF NOT EXISTS (SELECT 1 FROM RUTS WHERE Rut = 76123456)
    INSERT INTO RUTS (Rut, Nombre) VALUES (76123456, N'Camanchaca Cultivos Sur S.A.');

IF NOT EXISTS (SELECT 1 FROM RUTS WHERE Rut = 76789012)
    INSERT INTO RUTS (Rut, Nombre) VALUES (76789012, N'Aguas del Sur SpA');

IF NOT EXISTS (SELECT 1 FROM RUTS WHERE Rut = 76987654)
    INSERT INTO RUTS (Rut, Nombre) VALUES (76987654, N'Marina Austral Ltda.');

IF NOT EXISTS (SELECT 1 FROM INSTALACION WHERE Nombre = N'Planta Puerto Montt')
    INSERT INTO INSTALACION (Nombre) VALUES (N'Planta Puerto Montt');

IF NOT EXISTS (SELECT 1 FROM INSTALACION WHERE Nombre = N'Centro Calbuco')
    INSERT INTO INSTALACION (Nombre) VALUES (N'Centro Calbuco');

IF NOT EXISTS (SELECT 1 FROM INSTALACION WHERE Nombre = N'Centro Chiloe')
    INSERT INTO INSTALACION (Nombre) VALUES (N'Centro Chiloe');


/* =========================
   CUENCA
   ========================= */

IF NOT EXISTS (SELECT 1 FROM CUENCA WHERE nombre = N'Cuenca Rio Maullin')
    INSERT INTO CUENCA (nombre) VALUES (N'Cuenca Rio Maullin');

IF NOT EXISTS (SELECT 1 FROM CUENCA WHERE nombre = N'Cuencas e islas de Chiloe')
    INSERT INTO CUENCA (nombre) VALUES (N'Cuencas e islas de Chiloe');

DECLARE @cuencaMaullin INT = (
    SELECT id_cuenca FROM CUENCA WHERE nombre = N'Cuenca Rio Maullin'
);

DECLARE @cuencaChiloe INT = (
    SELECT id_cuenca FROM CUENCA WHERE nombre = N'Cuencas e islas de Chiloe'
);


/* =========================
   SUBCUENCA
   ========================= */

IF NOT EXISTS (SELECT 1 FROM SUBCUENCA WHERE nombre_subcuenca = N'Lago Llanquihue')
    INSERT INTO SUBCUENCA (
        fk_id_cuenca,
        nombre_subcuenca,
        shac,
        reserva,
        declaracion_agotamiento,
        zona_restriccion_superficial,
        zona_restriccion_subterranea,
        estadoshac,
        plan_gestion_hidrica
    )
    VALUES (
        @cuencaMaullin,
        N'Lago Llanquihue',
        N'SHAC-LLA-01',
        N'No',
        0,
        0,
        0,
        N'Vigente',
        1
    );

IF NOT EXISTS (SELECT 1 FROM SUBCUENCA WHERE nombre_subcuenca = N'Seno de Reloncavi')
    INSERT INTO SUBCUENCA (
        fk_id_cuenca,
        nombre_subcuenca,
        shac,
        reserva,
        declaracion_agotamiento,
        zona_restriccion_superficial,
        zona_restriccion_subterranea,
        estadoshac,
        plan_gestion_hidrica
    )
    VALUES (
        @cuencaMaullin,
        N'Seno de Reloncavi',
        N'SHAC-REL-02',
        N'No',
        0,
        0,
        0,
        N'Vigente',
        1
    );

IF NOT EXISTS (SELECT 1 FROM SUBCUENCA WHERE nombre_subcuenca = N'Estero Castro')
    INSERT INTO SUBCUENCA (
        fk_id_cuenca,
        nombre_subcuenca,
        shac,
        reserva,
        declaracion_agotamiento,
        zona_restriccion_superficial,
        zona_restriccion_subterranea,
        estadoshac,
        plan_gestion_hidrica
    )
    VALUES (
        @cuencaChiloe,
        N'Estero Castro',
        N'SHAC-CAS-03',
        N'No',
        0,
        0,
        0,
        N'Vigente',
        1
    );

DECLARE @subLlanquihue INT = (
    SELECT id_subcuenca FROM SUBCUENCA WHERE nombre_subcuenca = N'Lago Llanquihue'
);

DECLARE @subReloncavi INT = (
    SELECT id_subcuenca FROM SUBCUENCA WHERE nombre_subcuenca = N'Seno de Reloncavi'
);

DECLARE @subCastro INT = (
    SELECT id_subcuenca FROM SUBCUENCA WHERE nombre_subcuenca = N'Estero Castro'
);


/* =========================
   FUENTE
   ========================= */

IF NOT EXISTS (SELECT 1 FROM FUENTE WHERE nombre = N'Rio Maullin')
    INSERT INTO FUENTE (fk_id_subcuenca, nombre, tipo)
    VALUES (@subLlanquihue, N'Rio Maullin', N'Superficial');

IF NOT EXISTS (SELECT 1 FROM FUENTE WHERE nombre = N'Pozo Calbuco Norte')
    INSERT INTO FUENTE (fk_id_subcuenca, nombre, tipo)
    VALUES (@subReloncavi, N'Pozo Calbuco Norte', N'Subterranea');

IF NOT EXISTS (SELECT 1 FROM FUENTE WHERE nombre = N'Estero Gamboa')
    INSERT INTO FUENTE (fk_id_subcuenca, nombre, tipo)
    VALUES (@subCastro, N'Estero Gamboa', N'Superficial');

DECLARE @instPuertoMontt INT = (
    SELECT ID_Instalacion FROM INSTALACION WHERE Nombre = N'Planta Puerto Montt'
);

DECLARE @instCalbuco INT = (
    SELECT ID_Instalacion FROM INSTALACION WHERE Nombre = N'Centro Calbuco'
);

DECLARE @instChiloe INT = (
    SELECT ID_Instalacion FROM INSTALACION WHERE Nombre = N'Centro Chiloe'
);

DECLARE @fuenteMaullin INT = (
    SELECT id_fuente FROM FUENTE WHERE nombre = N'Rio Maullin'
);

DECLARE @fuenteCalbuco INT = (
    SELECT id_fuente FROM FUENTE WHERE nombre = N'Pozo Calbuco Norte'
);

DECLARE @fuenteGamboa INT = (
    SELECT id_fuente FROM FUENTE WHERE nombre = N'Estero Gamboa'
);


/* =========================
   DDAA
   ========================= */

IF NOT EXISTS (
    SELECT 1
    FROM DDAA
    WHERE nombre_fuente_derecho = N'Bocatoma Rio Maullin'
      AND fk_id_rut_titular = 76123456
)
    INSERT INTO DDAA (
        fk_id_comuna,
        fk_id_rut_titular,
        fk_id_instalacion,
        fk_id_fuente,
        nombre_fuente_derecho,
        naturaleza_derecho,
        tipo_derecho,
        estado_derecho
    )
    VALUES (
        N'10101',
        76123456,
        @instPuertoMontt,
        @fuenteMaullin,
        N'Bocatoma Rio Maullin',
        N'Consuntivo',
        N'Aprovechamiento',
        N'Vigente'
    );

IF NOT EXISTS (
    SELECT 1
    FROM DDAA
    WHERE nombre_fuente_derecho = N'Pozo profundo Calbuco Norte'
      AND fk_id_rut_titular = 76789012
)
    INSERT INTO DDAA (
        fk_id_comuna,
        fk_id_rut_titular,
        fk_id_instalacion,
        fk_id_fuente,
        nombre_fuente_derecho,
        naturaleza_derecho,
        tipo_derecho,
        estado_derecho
    )
    VALUES (
        N'10102',
        76789012,
        @instCalbuco,
        @fuenteCalbuco,
        N'Pozo profundo Calbuco Norte',
        N'Consuntivo',
        N'Exploracion',
        N'En revision'
    );

IF NOT EXISTS (
    SELECT 1
    FROM DDAA
    WHERE nombre_fuente_derecho = N'Captacion Estero Gamboa'
      AND fk_id_rut_titular = 76987654
)
    INSERT INTO DDAA (
        fk_id_comuna,
        fk_id_rut_titular,
        fk_id_instalacion,
        fk_id_fuente,
        nombre_fuente_derecho,
        naturaleza_derecho,
        tipo_derecho,
        estado_derecho
    )
    VALUES (
        N'10201',
        76987654,
        @instChiloe,
        @fuenteGamboa,
        N'Captacion Estero Gamboa',
        N'No consuntivo',
        N'Aprovechamiento',
        N'Vigente'
    );

DECLARE @ddaaMaullin INT = (
    SELECT id_ddaa
    FROM DDAA
    WHERE nombre_fuente_derecho = N'Bocatoma Rio Maullin'
      AND fk_id_rut_titular = 76123456
);

DECLARE @ddaaCalbuco INT = (
    SELECT id_ddaa
    FROM DDAA
    WHERE nombre_fuente_derecho = N'Pozo profundo Calbuco Norte'
      AND fk_id_rut_titular = 76789012
);

DECLARE @ddaaGamboa INT = (
    SELECT id_ddaa
    FROM DDAA
    WHERE nombre_fuente_derecho = N'Captacion Estero Gamboa'
      AND fk_id_rut_titular = 76987654
);


/* =========================
   DDAA_EXPEDIENTE
   ========================= */

IF NOT EXISTS (SELECT 1 FROM DDAA_EXPEDIENTE WHERE codigo = N'EXP-MAU-001')
    INSERT INTO DDAA_EXPEDIENTE (
        codigo,
        tipo,
        estado,
        resolucion_dga_n,
        resolucion_dga_fecha,
        resolucion_dga_link,
        catastro_n,
        catastro_fecha,
        catastro_link
    )
    VALUES (
        N'EXP-MAU-001',
        N'Constitucion',
        N'Aprobado',
        N'DGA-EXP-MAU-001',
        '2024-05-15',
        N'https://example.com/exp-mau-001',
        21001,
        '2024-06-01',
        N'https://example.com/catastro-21001'
    );

IF NOT EXISTS (SELECT 1 FROM DDAA_EXPEDIENTE WHERE codigo = N'EXP-CAL-002')
    INSERT INTO DDAA_EXPEDIENTE (
        codigo,
        tipo,
        estado,
        resolucion_dga_n,
        resolucion_dga_fecha,
        resolucion_dga_link,
        catastro_n,
        catastro_fecha,
        catastro_link
    )
    VALUES (
        N'EXP-CAL-002',
        N'Regularizacion',
        N'En tramite',
        N'DGA-EXP-CAL-002',
        '2024-05-15',
        N'https://example.com/exp-cal-002',
        21002,
        '2024-06-01',
        N'https://example.com/catastro-21002'
    );

IF NOT EXISTS (SELECT 1 FROM DDAA_EXPEDIENTE WHERE codigo = N'EXP-CAS-003')
    INSERT INTO DDAA_EXPEDIENTE (
        codigo,
        tipo,
        estado,
        resolucion_dga_n,
        resolucion_dga_fecha,
        resolucion_dga_link,
        catastro_n,
        catastro_fecha,
        catastro_link
    )
    VALUES (
        N'EXP-CAS-003',
        N'Traslado',
        N'Aprobado',
        N'DGA-EXP-CAS-003',
        '2024-05-15',
        N'https://example.com/exp-cas-003',
        21003,
        '2024-06-01',
        N'https://example.com/catastro-21003'
    );

DECLARE @expMaullin INT = (
    SELECT id FROM DDAA_EXPEDIENTE WHERE codigo = N'EXP-MAU-001'
);

DECLARE @expCalbuco INT = (
    SELECT id FROM DDAA_EXPEDIENTE WHERE codigo = N'EXP-CAL-002'
);

DECLARE @expGamboa INT = (
    SELECT id FROM DDAA_EXPEDIENTE WHERE codigo = N'EXP-CAS-003'
);


/* =========================
   RELACION DDAA - EXPEDIENTE
   ========================= */

IF NOT EXISTS (
    SELECT 1
    FROM DDAA_DDAA_EXPEDIENTE
    WHERE id_ddaa = @ddaaMaullin
      AND id_expediente = @expMaullin
)
    INSERT INTO DDAA_DDAA_EXPEDIENTE (id_ddaa, id_expediente)
    VALUES (@ddaaMaullin, @expMaullin);

IF NOT EXISTS (
    SELECT 1
    FROM DDAA_DDAA_EXPEDIENTE
    WHERE id_ddaa = @ddaaCalbuco
      AND id_expediente = @expCalbuco
)
    INSERT INTO DDAA_DDAA_EXPEDIENTE (id_ddaa, id_expediente)
    VALUES (@ddaaCalbuco, @expCalbuco);

IF NOT EXISTS (
    SELECT 1
    FROM DDAA_DDAA_EXPEDIENTE
    WHERE id_ddaa = @ddaaGamboa
      AND id_expediente = @expGamboa
)
    INSERT INTO DDAA_DDAA_EXPEDIENTE (id_ddaa, id_expediente)
    VALUES (@ddaaGamboa, @expGamboa);


/* =========================
   DDAA_EJERCICIO
   ========================= */

IF NOT EXISTS (SELECT 1 FROM DDAA_EJERCICIO WHERE fk_id_ddaa = @ddaaMaullin)
    INSERT INTO DDAA_EJERCICIO (
        fk_id_ddaa,
        ejercicio_derecho,
        continuidad_derecho
    )
    VALUES (
        @ddaaMaullin,
        N'Ejercicio permanente',
        N'Continuo'
    );

IF NOT EXISTS (SELECT 1 FROM DDAA_EJERCICIO WHERE fk_id_ddaa = @ddaaCalbuco)
    INSERT INTO DDAA_EJERCICIO (
        fk_id_ddaa,
        ejercicio_derecho,
        continuidad_derecho
    )
    VALUES (
        @ddaaCalbuco,
        N'Ejercicio provisional',
        N'Discontinuo'
    );

IF NOT EXISTS (SELECT 1 FROM DDAA_EJERCICIO WHERE fk_id_ddaa = @ddaaGamboa)
    INSERT INTO DDAA_EJERCICIO (
        fk_id_ddaa,
        ejercicio_derecho,
        continuidad_derecho
    )
    VALUES (
        @ddaaGamboa,
        N'Ejercicio estacional',
        N'Continuo'
    );

DECLARE @ejMaullin INT = (
    SELECT TOP 1 id_ejercicio
    FROM DDAA_EJERCICIO
    WHERE fk_id_ddaa = @ddaaMaullin
);

DECLARE @ejCalbuco INT = (
    SELECT TOP 1 id_ejercicio
    FROM DDAA_EJERCICIO
    WHERE fk_id_ddaa = @ddaaCalbuco
);

DECLARE @ejGamboa INT = (
    SELECT TOP 1 id_ejercicio
    FROM DDAA_EJERCICIO
    WHERE fk_id_ddaa = @ddaaGamboa
);


/* =========================
   DDAA_CAUDAL
   ========================= */

IF NOT EXISTS (
    SELECT 1
    FROM DDAA_CAUDAL
    WHERE fk_id_ejercicio = @ejMaullin
      AND mes = 1
)
    INSERT INTO DDAA_CAUDAL (
        fk_id_ejercicio,
        mes,
        caudal_mensual
    )
    VALUES (
        @ejMaullin,
        1,
        12.5000
    );

IF NOT EXISTS (
    SELECT 1
    FROM DDAA_CAUDAL
    WHERE fk_id_ejercicio = @ejCalbuco
      AND mes = 1
)
    INSERT INTO DDAA_CAUDAL (
        fk_id_ejercicio,
        mes,
        caudal_mensual
    )
    VALUES (
        @ejCalbuco,
        1,
        8.7500
    );

IF NOT EXISTS (
    SELECT 1
    FROM DDAA_CAUDAL
    WHERE fk_id_ejercicio = @ejGamboa
      AND mes = 1
)
    INSERT INTO DDAA_CAUDAL (
        fk_id_ejercicio,
        mes,
        caudal_mensual
    )
    VALUES (
        @ejGamboa,
        1,
        15.2000
    );


/* =========================
   DDAA_PAGO_NO_USO
   ========================= */

IF NOT EXISTS (SELECT 1 FROM DDAA_PAGO_NO_USO WHERE folio_tgr = 1001)
    INSERT INTO DDAA_PAGO_NO_USO (
        folio_tgr,
        fk_id_ddaa,
        fecha_cobro,
        caudal_aplicado_ls,
        factor_aplicado,
        patente_utm,
        patente_clp
    )
    VALUES (
        1001,
        @ddaaMaullin,
        '2025-01-20',
        12.5000,
        2,
        1.7500,
        115000.00
    );

IF NOT EXISTS (SELECT 1 FROM DDAA_PAGO_NO_USO WHERE folio_tgr = 1002)
    INSERT INTO DDAA_PAGO_NO_USO (
        folio_tgr,
        fk_id_ddaa,
        fecha_cobro,
        caudal_aplicado_ls,
        factor_aplicado,
        patente_utm,
        patente_clp
    )
    VALUES (
        1002,
        @ddaaCalbuco,
        '2025-01-20',
        8.7500,
        2,
        1.2000,
        82000.00
    );

IF NOT EXISTS (SELECT 1 FROM DDAA_PAGO_NO_USO WHERE folio_tgr = 1003)
    INSERT INTO DDAA_PAGO_NO_USO (
        folio_tgr,
        fk_id_ddaa,
        fecha_cobro,
        caudal_aplicado_ls,
        factor_aplicado,
        patente_utm,
        patente_clp
    )
    VALUES (
        1003,
        @ddaaGamboa,
        '2025-01-20',
        15.2000,
        2,
        2.1000,
        142000.00
    );


/* =========================
   VALIDACION FINAL
   ========================= */

SELECT
    (SELECT COUNT(*) FROM COMUNA) AS comunas,
    (SELECT COUNT(*) FROM RUTS) AS titulares,
    (SELECT COUNT(*) FROM INSTALACION) AS instalaciones,
    (SELECT COUNT(*) FROM FUENTE) AS fuentes,
    (SELECT COUNT(*) FROM DDAA) AS ddaa;