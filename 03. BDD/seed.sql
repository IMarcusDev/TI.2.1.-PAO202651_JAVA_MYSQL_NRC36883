-- =============================================================
-- Sistema CountryAPI · Datos de prueba (MySQL 8)
-- Ejecutar después de init.sql.
-- Autor: Escobar Marcos · 21/11/2026
-- =============================================================
USE api_paises;

-- Usuarios de prueba (clave en SHA-256):
--   monster / monster9
--   admin   / admin123
INSERT INTO usuario (username, password_hash, nombre, estado) VALUES
  ('monster', '774e993500f4027acfd72b7a7ee564b76ae43cf7c4c943ed0e0f364cca16b6ec', 'Marcos Escobar', 'ACTIVO'),
  ('admin',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrador',  'ACTIVO');

-- Regiones de la API REST Countries traducidas al español
INSERT INTO region (nombre) VALUES
  ('América'), ('Europa'), ('Asia'), ('África'), ('Oceanía'), ('Antártida');

-- Muestra de países para probar el sistema sin conexión a la API.
-- La opción «Importar países» actualiza estos registros y agrega el resto.
INSERT INTO pais (nombre, capital, id_region, subregion, poblacion, area, moneda) VALUES
  ('Ecuador',   'Quito',     (SELECT id_region FROM region WHERE nombre = 'América'), 'South America',             17643060,   276841,  'United States dollar (USD)'),
  ('Colombia',  'Bogotá',    (SELECT id_region FROM region WHERE nombre = 'América'), 'South America',             50882884,   1141748, 'Colombian peso (COP)'),
  ('Perú',      'Lima',      (SELECT id_region FROM region WHERE nombre = 'América'), 'South America',             32971846,   1285216, 'Peruvian sol (PEN)'),
  ('Brasil',    'Brasília',  (SELECT id_region FROM region WHERE nombre = 'América'), 'South America',             212559409,  8515767, 'Brazilian real (BRL)'),
  ('España',    'Madrid',    (SELECT id_region FROM region WHERE nombre = 'Europa'), 'Southern Europe',           47351567,   505992,  'Euro (EUR)'),
  ('Alemania',  'Berlin',    (SELECT id_region FROM region WHERE nombre = 'Europa'), 'Western Europe',            83240525,   357114,  'Euro (EUR)'),
  ('China',     'Beijing',   (SELECT id_region FROM region WHERE nombre = 'Asia'), 'Eastern Asia',              1402112000, 9706961, 'Chinese yuan (CNY)'),
  ('India',     'New Delhi', (SELECT id_region FROM region WHERE nombre = 'Asia'), 'Southern Asia',             1380004385, 3287590, 'Indian rupee (INR)'),
  ('Japón',     'Tokyo',     (SELECT id_region FROM region WHERE nombre = 'Asia'), 'Eastern Asia',              125836021,  377930,  'Japanese yen (JPY)'),
  ('Nigeria',   'Abuja',     (SELECT id_region FROM region WHERE nombre = 'África'), 'Western Africa',            206139587,  923768,  'Nigerian naira (NGN)'),
  ('Egipto',    'Cairo',     (SELECT id_region FROM region WHERE nombre = 'África'), 'Northern Africa',           102334403,  1002450, 'Egyptian pound (EGP)'),
  ('Australia', 'Canberra',  (SELECT id_region FROM region WHERE nombre = 'Oceanía'), 'Australia and New Zealand', 25687041,   7692024, 'Australian dollar (AUD)');

-- Muestra de universidades de Ecuador (actividad avanzada).
-- La opción «Universidades» → «Importar» actualiza estos registros y agrega el resto.
INSERT INTO universidad (nombre, dominio, sitio_web, pais) VALUES
  ('Escuela Politécnica del Ejército',            'espe.edu.ec', 'http://www.espe.edu.ec/', 'Ecuador'),
  ('Escuela Politécnica Nacional',                'epn.edu.ec',  'http://www.epn.edu.ec/',  'Ecuador'),
  ('Universidad Central del Ecuador',             'uce.edu.ec',  'http://www.uce.edu.ec/',  'Ecuador'),
  ('Pontificia Universidad Católica del Ecuador', 'puce.edu.ec', 'http://www.puce.edu.ec/', 'Ecuador'),
  ('Universidad San Francisco de Quito',          'usfq.edu.ec', 'http://www.usfq.edu.ec/', 'Ecuador');
