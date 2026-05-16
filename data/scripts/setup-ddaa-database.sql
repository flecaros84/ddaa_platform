/*
   Ejecutar en SQL Server con un usuario administrador, por ejemplo desde SSMS.
   Este script prepara la base local usada por ddaa-service.
*/

USE [master];

IF DB_ID(N'ddaa') IS NULL
BEGIN
    CREATE DATABASE [ddaa];
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.server_principals
    WHERE name = N'ddaa_user'
)
BEGIN
    CREATE LOGIN [ddaa_user] WITH PASSWORD = N'admin', CHECK_POLICY = OFF;
END;

ALTER LOGIN [ddaa_user] WITH DEFAULT_DATABASE = [ddaa];

USE [ddaa];

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE name = N'ddaa_user'
)
BEGIN
    CREATE USER [ddaa_user] FOR LOGIN [ddaa_user];
END;
ELSE
BEGIN
    ALTER USER [ddaa_user] WITH LOGIN = [ddaa_user];
END;

IF IS_ROLEMEMBER(N'db_datareader', N'ddaa_user') = 0
BEGIN
    ALTER ROLE [db_datareader] ADD MEMBER [ddaa_user];
END;

IF IS_ROLEMEMBER(N'db_datawriter', N'ddaa_user') = 0
BEGIN
    ALTER ROLE [db_datawriter] ADD MEMBER [ddaa_user];
END;

IF IS_ROLEMEMBER(N'db_ddladmin', N'ddaa_user') = 0
BEGIN
    ALTER ROLE [db_ddladmin] ADD MEMBER [ddaa_user];
END;

IF IS_ROLEMEMBER(N'db_owner', N'ddaa_user') = 0
BEGIN
    ALTER ROLE [db_owner] ADD MEMBER [ddaa_user];
END;

GRANT VIEW DATABASE STATE TO [ddaa_user];
GRANT VIEW DATABASE PERFORMANCE STATE TO [ddaa_user];

SELECT
    HAS_PERMS_BY_NAME(DB_NAME(), 'DATABASE', 'VIEW DATABASE STATE') AS has_view_database_state,
    HAS_PERMS_BY_NAME(DB_NAME(), 'DATABASE', 'VIEW DATABASE PERFORMANCE STATE') AS has_view_database_performance_state,
    IS_ROLEMEMBER(N'db_owner', N'ddaa_user') AS is_db_owner;
