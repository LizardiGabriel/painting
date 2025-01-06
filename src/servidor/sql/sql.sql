    DROP DATABASE IF EXISTS painting;
    CREATE DATABASE painting;
    USE painting;

        CREATE TABLE Users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user VARCHAR(255) NOT NULL,
            password VARCHAR(255) NOT NULL,
            type ENUM('admin', 'judge', 'painter', 'president') NOT NULL,
            nombre VARCHAR(255)
        );

-- Resto de las tablas especializadas (jueces, pintores, presidentes)

        CREATE TABLE Judges (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            clave_publica_rsaOAP VARCHAR(400) NOT NULL,
            FOREIGN KEY (user_id) REFERENCES Users(id)
        );

        CREATE TABLE Painters (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            clave_publica_ecdsa VARCHAR(255) NOT NULL,
            FOREIGN KEY (user_id) REFERENCES Users(id)
        );

        CREATE TABLE Presidents (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            FOREIGN KEY (user_id) REFERENCES Users(id)
        );


insert into Users (user, password, type, nombre) values ('painter', 'painter', 'painter', 'painter');
insert into Users (user, password, type, nombre) values ('judge', 'judge', 'judge', 'judge');
insert into Users (user, password, type, nombre) values ('president', 'president', 'president', 'president');
        
        
        
        
        select * from Users;
        
        select * from Judges;
        
        select * from Painters;










