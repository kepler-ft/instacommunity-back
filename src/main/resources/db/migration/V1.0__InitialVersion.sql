CREATE TABLE users (
    id VARCHAR PRIMARY KEY,
    name VARCHAR NOT NULL,
    username VARCHAR NOT NULL,
    occupation VARCHAR,
    about VARCHAR,
    email VARCHAR NOT NULL,
    contact_title VARCHAR,
    contact_link VARCHAR,
    photo_url VARCHAR
);

CREATE TYPE community_type AS ENUM ('OPEN', 'MODERATED', 'MANAGED');

CREATE TABLE communities (
     id SERIAL PRIMARY KEY,
     name VARCHAR(200) NOT NULL,
     description TEXT NOT NULL,
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

CREATE TABLE community_contacts (
    id SERIAL PRIMARY KEY,
    community_id INT NOT NULL,
    title VARCHAR NOT NULL,
    link VARCHAR,
    FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE
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

INSERT INTO users(id, name, username, occupation, about, email, contact_title, contact_link, photo_url)
VALUES ('batatinhafrita123', 'Ada Lovelace', 'LoveAda', 'Programadora', 'matemática e escritora inglesa',
    'ada@example.com', 'github','https://github.com/kepler-ft', 'https://i.imgur.com/5suSRAj.png');

-- /communities/getting%20started
INSERT INTO communities(name, description, admin, slug, photo_url, type)
VALUES ('Getting Started',
        'Kepler-42, anteriormente conhecido como KOI-961, ' ||
        'é uma estrela anã vermelha localizada na constelação de ' ||
        'Cygnus a cerca de 126 anos-luz a partir do Sol. ' ||
        'Ela tem três planetas extrassolares conhecidos, ' ||
        'os quais tem raios menores do que o raio da Terra, e, ' ||
        'provavelmente, também são menores em massa.', 'batatinhafrita123',
        'getting-started',
        'https://pt.wikipedia.org/wiki/Kepler-42#/media/Ficheiro:Artist''s_conception_of_Kepler-42.jpg', 'OPEN');

INSERT INTO communities_followers(user_id, community_id)
VALUES ('batatinhafrita123', 1);

INSERT INTO tags (name)
VALUES ('Boas-vindas');

INSERT INTO communities_tags (tag_id, community_id)
VALUES (1, 1);

INSERT INTO community_contacts (title, link, community_id)
VALUES ('github', 'https://github.com/kepler-ft', 1);
