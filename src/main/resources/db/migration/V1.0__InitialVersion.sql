CREATE TABLE users (
    id VARCHAR PRIMARY KEY,
    name VARCHAR NOT NULL,
    username VARCHAR NOT NULL,
    occupation VARCHAR,
    about VARCHAR,
    email VARCHAR NOT NULL,
    contact VARCHAR,
    photo_url VARCHAR
);

CREATE TYPE community_type AS ENUM ('open', 'moderated', 'managed');

CREATE TABLE communities (
     id SERIAL PRIMARY KEY,
     name VARCHAR(200) NOT NULL,
     description TEXT NOT NULL,
     contact VARCHAR(200) NOT NULL,
     contact2 VARCHAR(200) DEFAULT '',
     contact3 VARCHAR(200) DEFAULT '',
     admin VARCHAR,
     slug VARCHAR NOT NULL,
     photo_url VARCHAR,
     type community_type,
     FOREIGN KEY (admin) REFERENCES users(id)
);

CREATE TABLE communities_followers (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR NOT NULL,
    community_id INT NOT NULL,
    approved BOOLEAN DEFAULT true,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    UNIQUE (user_id, community_id)
);

CREATE TABLE communities_moderators (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR NOT NULL,
    community_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    UNIQUE (user_id, community_id)
);

CREATE TABLE tags (
    id SERIAL PRIMARY KEY ,
    name VARCHAR UNIQUE NOT NULL
);

CREATE TABLE communities_tags (
    id SERIAL,
    tag_id INT,
    community_id INT,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    UNIQUE (tag_id, community_id)
);

INSERT INTO users(id, name, username, occupation, about, email, contact, photo_url)
VALUES ('batatinhafrita123', 'Ada Lovelace', 'LoveAda', 'Programadora', 'matemática e escritora inglesa',
    'ada@example.com', 'https://github.com/kepler-ft', 'https://i.imgur.com/5suSRAj.png');

-- /communities/getting%20started
INSERT INTO communities(name, contact, description, admin, slug, photo_url, type)
VALUES ('Getting Started', 'https://github.com/kepler-ft',
        'Kepler-42, anteriormente conhecido como KOI-961, ' ||
        'é uma estrela anã vermelha localizada na constelação de ' ||
        'Cygnus a cerca de 126 anos-luz a partir do Sol. ' ||
        'Ela tem três planetas extrassolares conhecidos, ' ||
        'os quais tem raios menores do que o raio da Terra, e, ' ||
        'provavelmente, também são menores em massa.', 'batatinhafrita123',
        'getting-started',
        'https://pt.wikipedia.org/wiki/Kepler-42#/media/Ficheiro:Artist''s_conception_of_Kepler-42.jpg', 'open');

INSERT INTO communities_followers(user_id, community_id)
VALUES ('batatinhafrita123', 1);

INSERT INTO tags (name)
VALUES ('Boas-vindas');

INSERT INTO communities_tags (tag_id, community_id)
VALUES (1, 1);