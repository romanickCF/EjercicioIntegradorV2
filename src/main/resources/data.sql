USE integrador;

-- 1. Empresas (IDs 1 a 5)
INSERT INTO Empresa (id, nombre, sector, web, fechaRegistro) VALUES
                                                                 (1, 'Indra', 'Consultoría TI', 'https://www.indracompany.com', '2026-01-15'),
                                                                 (2, 'Accenture', 'Consultoría y Tecnología', 'https://www.accenture.com', '2026-02-10'),
                                                                 (3, 'Capgemini', 'Servicios TI', 'https://www.capgemini.com', '2026-03-05'),
                                                                 (4, 'NTT Data', 'Consultoría tecnológica', 'https://es.nttdata.com', '2026-03-20'),
                                                                 (5, 'Telefonica', 'Telecomunicaciones', 'https://www.telefonica.com', '2026-04-01'),
                                                                 (6, 'BBVA', 'Banca y Servicios Financieros', 'https://www.bbva.com', '2026-04-12');

-- 2. Ofertas (IDs 1 a 10, referenciando Empresa.id)
INSERT INTO Oferta (id, id_empresa, titulo, descripcion, estado, fechaPostulacion, enlace, salario, notas) VALUES
                                                                                                               (1, 1, 'Desarrollador Java Junior', 'Desarrollo de microservicios y mantenimiento de aplicaciones empresariales en Java.', 'pendiente', '2026-10-26', 'https://empleo.example.com/indra/1001', 26000.00, 'Enviado por el portal de empleo'),
                                                                                                               (2, 2, 'Backend Developer', 'Construcción de APIs REST y servicios backend en entorno cloud.', 'entrevista', '2026-10-20', 'https://empleo.example.com/accenture/2002', 31000.00, 'Contacto por LinkedIn con la recruiter'),
                                                                                                               (3, 3, 'Analista Programador', 'Análisis funcional y programación en proyectos corporativos.', 'rechazado', '2026-10-12', 'https://empleo.example.com/capgemini/3003', 29000.00, 'Piden mas de 3 anos de experiencia'),
                                                                                                               (4, 4, 'Desarrollador Full Stack', 'Desarrollo frontend y backend con arquitectura moderna.', 'aceptado', '2026-10-05', 'https://empleo.example.com/nttdata/4004', 33000.00, 'Oferta recibida tras la segunda entrevista'),
                                                                                                               (5, 5, 'Practicas DAM', 'Prácticas curriculares de desarrollo de aplicaciones multiplataforma.', 'pendiente', '2026-10-24', NULL, 900.00, 'Sin enlace, solicitado por email'),
                                                                                                               (6, 6, 'Ingeniero de Software', 'Diseño y evolución de plataformas críticas bancarias.', 'entrevista', '2026-10-15', 'https://empleo.example.com/bbva/6006', 36000.00, 'Prueba tecnica pendiente de entregar'),
                                                                                                               (7, 1, 'Desarrollador Spring Boot', 'Desarrollo de servicios backend con Spring Boot y bases de datos relacionales.', 'rechazado', '2026-09-30', 'https://empleo.example.com/indra/7007', 28500.00, 'Proceso cerrado sin feedback'),
                                                                                                               (8, 2, 'QA Automation Engineer', 'Automatización de pruebas funcionales y de regresión.', 'pendiente', '2026-10-22', 'https://empleo.example.com/accenture/8008', 27500.00, 'Esperando respuesta'),
                                                                                                               (9, 3, 'Desarrollador Java Senior', 'Arquitectura y liderazgo técnico en sistemas Java.', 'entrevista', '2026-10-08', 'https://empleo.example.com/capgemini/9009', 42000.00, 'Entrevista tecnica con el equipo de arquitectura'),
                                                                                                               (10, 4, 'Becario Desarrollo Web', 'Apoyo en desarrollo web y maquetación.', 'aceptado', '2026-09-18', 'https://empleo.example.com/nttdata/1010', 1100.00, 'Empiezo en noviembre');

-- 3. Entrevistas (referenciando Oferta.id)
INSERT INTO Entrevista (id, id_oferta, fecha, tipo, notas, resultado) VALUES
                                                                          (1, 2, '2026-10-23 10:30:00', 'RRHH', 'Primera toma de contacto y revisión de expectativas salariales.', 'superada'),
                                                                          (2, 2, '2026-10-28 16:00:00', 'Técnica', 'Entrevista técnica con el equipo de backend.', 'pendiente'),
                                                                          (3, 4, '2026-10-09 11:00:00', 'RRHH', 'Presentación del proyecto y modalidad híbrida.', 'superada'),
                                                                          (4, 4, '2026-10-14 15:30:00', 'Técnica', 'Resolución de caso práctico y preguntas de arquitectura.', 'superada'),
                                                                          (5, 6, '2026-10-19 12:00:00', 'Técnica', 'Explicación de prueba técnica y diseño de sistemas.', 'pendiente'),
                                                                          (6, 9, '2026-10-13 09:30:00', 'Arquitectura', 'Revisión de patrones, concurrencia y experiencia previa.', 'superada'),
                                                                          (7, 10, '2026-09-22 17:00:00', 'RRHH', 'Encaje para incorporación en prácticas.', 'superada');