DROP DATABASE IF EXISTS painting;
CREATE DATABASE painting;
USE painting;

CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    type ENUM('admin', 'judge', 'painter', 'president') NOT NULL,
    nombre VARCHAR(255)
);

CREATE TABLE Judges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    public_key_rsa_oaep TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

CREATE TABLE Painters (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    public_key_ecdsa TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

CREATE TABLE Presidents (

    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    public_key_rsa TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

CREATE TABLE Paintings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    painter_id INT NOT NULL,
    encrypted_painting_data VARCHAR(255),
    iv VARCHAR(255) NOT NULL, -- IV utilizado para AES-GCM
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (painter_id) REFERENCES Painters(id)
);

-- Tabla para almacenar las claves AES cifradas para cada juez
CREATE TABLE Encrypted_AES_Keys (
    id INT AUTO_INCREMENT PRIMARY KEY,
    painting_id INT NOT NULL,
    judge_id INT NOT NULL,
    encrypted_aes_key TEXT NOT NULL,
    FOREIGN KEY (painting_id) REFERENCES Paintings(id),
    FOREIGN KEY (judge_id) REFERENCES Judges(id)
);

CREATE TABLE Evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    painting_id INT NOT NULL,
    judge_id INT NOT NULL,
    stars INT NOT NULL CHECK (stars BETWEEN 1 AND 3),
    comments TEXT,
    is_evaluated bool,
    blinded_message TEXT, -- Nuevo
    inv TEXT, -- Representación base64 del inverso de r.
    blind_signature TEXT, -- Firma ciega del presidente
    evaluation_signature TEXT, -- Firma de la evaluación por el juez
    FOREIGN KEY (painting_id) REFERENCES Paintings(id),
    FOREIGN KEY (judge_id) REFERENCES Judges(id)
);

-- Tabla para almacenar el resultado final de cada pintura
CREATE TABLE Painting_Results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    painting_id INT NOT NULL UNIQUE,
    total_stars INT NOT NULL,
    FOREIGN KEY (painting_id) REFERENCES Paintings(id)
);



select * from Users;
select * from Painters;
select * from Paintings;
select * from Presidents;

select * from Evaluations;

select * from Encrypted_AES_Keys;

SELECT id, type, nombre FROM Users WHERE user = 'painter1' AND password = 'painter1';

