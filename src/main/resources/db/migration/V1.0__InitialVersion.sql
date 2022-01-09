CREATE TABLE users (
    id varchar PRIMARY KEY,
    name VARCHAR NOT NULL,
    username VARCHAR NOT NULL,
    occupation VARCHAR,
    email VARCHAR NOT NULL,
    use_photo BOOLEAN
);

CREATE TABLE communities (
     id SERIAL PRIMARY KEY,
     name VARCHAR(200) NOT NULL,
     description TEXT NOT NULL,
     contact VARCHAR(200) NOT NULL,
     contact2 VARCHAR(200) DEFAULT '',
     contact3 VARCHAR(200) DEFAULT '',
     creator INTEGER
);

CREATE TABLE users_communities (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    community_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (community_id) REFERENCES communities(id),
    UNIQUE (user_id, community_id)
);

INSERT INTO users(google_id, name, username, occupation, email, use_photo)
VALUES ('batatinhafrita123', 'Ada Luvlace', 'ada', 'Programadora', 'ada@example.com', false);

INSERT INTO communities(name, contact, description, creator)
VALUES ('Getting Started', 'https://github.com/kepler-ft',
        'Kepler-42, anteriormente conhecido como KOI-961, ' ||
        'é uma estrela anã vermelha localizada na constelação de ' ||
        'Cygnus a cerca de 126 anos-luz a partir do Sol. ' ||
        'Ela tem três planetas extrassolares conhecidos, ' ||
        'os quais tem raios menores do que o raio da Terra, e, ' ||
        'provavelmente, também são menores em massa.', 1);

INSERT INTO users_communities(user_id, community_id)
VALUES (1, 1);