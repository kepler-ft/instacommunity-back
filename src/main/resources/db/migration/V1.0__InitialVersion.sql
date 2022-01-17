CREATE TABLE users (
    id VARCHAR PRIMARY KEY,
    name VARCHAR NOT NULL,
    username VARCHAR NOT NULL,
    occupation VARCHAR,
    email VARCHAR NOT NULL,
    use_photo BOOLEAN,
    photo_url VARCHAR
);

CREATE TABLE communities (
     id SERIAL PRIMARY KEY,
     name VARCHAR(200) NOT NULL,
     description TEXT NOT NULL,
     contact VARCHAR(200) NOT NULL,
     contact2 VARCHAR(200) DEFAULT '',
     contact3 VARCHAR(200) DEFAULT '',
     creator VARCHAR,
     FOREIGN KEY (creator) REFERENCES users(id) ON DELETE NO ACTION
);

CREATE TABLE users_communities (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR NOT NULL,
    community_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    UNIQUE (user_id, community_id)
);

INSERT INTO users(id, name, username, occupation, email, use_photo, photo_url)
VALUES ('batatinhafrita123', 'Ada Luvlace', 'ada', 'Programadora', 'ada@example.com', false, '');

INSERT INTO communities(name, contact, description, creator)
VALUES ('Getting Started', 'https://github.com/kepler-ft',
        'Kepler-42, anteriormente conhecido como KOI-961, ' ||
        'é uma estrela anã vermelha localizada na constelação de ' ||
        'Cygnus a cerca de 126 anos-luz a partir do Sol. ' ||
        'Ela tem três planetas extrassolares conhecidos, ' ||
        'os quais tem raios menores do que o raio da Terra, e, ' ||
        'provavelmente, também são menores em massa.', 'batatinhafrita123');

INSERT INTO users_communities(user_id, community_id)
VALUES ('batatinhafrita123', 1);
