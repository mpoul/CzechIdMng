CREATE TABLE idm_configuration (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    name character varying(255) NOT NULL,
    secured boolean,
    value character varying(255)
);

CREATE TABLE idm_console_log (
    id bigint NOT NULL
);


CREATE TABLE idm_email_log (
    id bigint NOT NULL
);

CREATE TABLE idm_identity (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    description character varying(255),
    disabled boolean NOT NULL,
    email character varying(255),
    first_name character varying(255),
    last_name character varying(255) NOT NULL,
    password bytea,
    phone character varying(30),
    title_after character varying(100),
    title_before character varying(100),
    username character varying(255) NOT NULL,
    version bigint
);

CREATE TABLE idm_identity_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    description character varying(255),
    disabled boolean,
    email character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    phone character varying(30),
    title_after character varying(100),
    title_before character varying(100),
    username character varying(255)
);

CREATE TABLE idm_identity_role (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    valid_from date,
    valid_till date,
    identity_id bigint NOT NULL,
    role_id bigint NOT NULL
);

CREATE TABLE idm_identity_role_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    valid_from date,
    valid_till date,
    identity_id bigint,
    role_id bigint
);

CREATE TABLE idm_identity_working_position (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    "position" character varying(255),
    valid_from date,
    valid_till date,
    identity_id bigint NOT NULL,
    manager_id bigint,
    organization_id bigint
);

CREATE TABLE idm_identity_working_position_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    "position" character varying(255),
    valid_from date,
    valid_till date,
    identity_id bigint,
    manager_id bigint,
    organization_id bigint
);

CREATE TABLE idm_notification (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    html_message character varying(255),
    subject character varying(255),
    text_message character varying(255),
    sent timestamp without time zone,
    sent_log character varying(2000),
    parent_notification_id bigint,
    sender_id bigint
);

CREATE TABLE idm_notification_log (
    topic character varying(255),
    id bigint NOT NULL
);

CREATE TABLE idm_notification_recipient (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    real_recipient character varying(255),
    identity_recipient_id bigint,
    notification_id bigint NOT NULL
);

CREATE TABLE idm_organization (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    disabled boolean NOT NULL,
    name character varying(255) NOT NULL,
    version bigint,
    parent_id bigint
);

CREATE TABLE idm_organization_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    disabled boolean,
    name character varying(255),
    parent_id bigint
);

CREATE TABLE idm_role (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    approve_add_workflow character varying(255),
    approve_remove_workflow character varying(255),
    description character varying(255),
    disabled boolean NOT NULL,
    name character varying(255) NOT NULL,
    role_type character varying(255) NOT NULL,
    version bigint
);

CREATE TABLE idm_role_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    approve_add_workflow character varying(255),
    approve_remove_workflow character varying(255),
    description character varying(255),
    disabled boolean,
    name character varying(255),
    role_type character varying(255)
);

CREATE TABLE idm_role_authority (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    action_permission character varying(255) NOT NULL,
    target_permission character varying(255) NOT NULL,
    role_id bigint NOT NULL
);

CREATE TABLE idm_role_authority_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    action_permission character varying(255),
    target_permission character varying(255),
    role_id bigint
);

CREATE TABLE idm_role_composition (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    sub_id bigint NOT NULL,
    superior_id bigint NOT NULL
);

CREATE TABLE idm_role_composition_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    sub_id bigint,
    superior_id bigint
);

CREATE TABLE revinfo (
    rev integer NOT NULL,
    revtstmp bigint
);

ALTER TABLE ONLY idm_configuration
    ADD CONSTRAINT idm_configuration_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_console_log
    ADD CONSTRAINT idm_console_log_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_email_log
    ADD CONSTRAINT idm_email_log_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_identity_aud
    ADD CONSTRAINT idm_identity_aud_pkey PRIMARY KEY (id, rev);

ALTER TABLE ONLY idm_identity
    ADD CONSTRAINT idm_identity_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_identity_role_aud
    ADD CONSTRAINT idm_identity_role_aud_pkey PRIMARY KEY (id, rev);

ALTER TABLE ONLY idm_identity_role
    ADD CONSTRAINT idm_identity_role_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_identity_working_position_aud
    ADD CONSTRAINT idm_identity_working_position_aud_pkey PRIMARY KEY (id, rev);

ALTER TABLE ONLY idm_identity_working_position
    ADD CONSTRAINT idm_identity_working_position_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_notification_log
    ADD CONSTRAINT idm_notification_log_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_notification
    ADD CONSTRAINT idm_notification_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_notification_recipient
    ADD CONSTRAINT idm_notification_recipient_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_organization_aud
    ADD CONSTRAINT idm_organization_aud_pkey PRIMARY KEY (id, rev);

ALTER TABLE ONLY idm_organization
    ADD CONSTRAINT idm_organization_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_role_aud
    ADD CONSTRAINT idm_role_aud_pkey PRIMARY KEY (id, rev);

ALTER TABLE ONLY idm_role_authority_aud
    ADD CONSTRAINT idm_role_authority_aud_pkey PRIMARY KEY (id, rev);

ALTER TABLE ONLY idm_role_authority
    ADD CONSTRAINT idm_role_authority_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_role_composition_aud
    ADD CONSTRAINT idm_role_composition_aud_pkey PRIMARY KEY (id, rev);

ALTER TABLE ONLY idm_role_composition
    ADD CONSTRAINT idm_role_composition_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idm_role
    ADD CONSTRAINT idm_role_pkey PRIMARY KEY (id);

ALTER TABLE ONLY revinfo
    ADD CONSTRAINT revinfo_pkey PRIMARY KEY (rev);

ALTER TABLE ONLY idm_configuration
    ADD CONSTRAINT uk_59n5k8ygnaip1pefj02e3eqla UNIQUE (name);

ALTER TABLE ONLY idm_identity
    ADD CONSTRAINT uk_5aqknnxk13ycwr46tpbl6j1tt UNIQUE (username);

ALTER TABLE ONLY idm_role
    ADD CONSTRAINT uk_rf8ejw5hpvy7eewpnijmoeh32 UNIQUE (name);

CREATE INDEX ux_organization_name ON idm_organization USING btree (name);

CREATE INDEX ux_role_name ON idm_role USING btree (name);

ALTER TABLE ONLY idm_organization
    ADD CONSTRAINT fk_2n1u7nfur91wasj0csui0jbhs FOREIGN KEY (parent_id) REFERENCES idm_organization(id);

ALTER TABLE ONLY idm_role_composition
    ADD CONSTRAINT fk_4ug07v1wi3g1ai54k8uj0qekb FOREIGN KEY (sub_id) REFERENCES idm_role(id);

ALTER TABLE ONLY idm_notification_log
    ADD CONSTRAINT fk_6lxo8e33m2cn2kemxjfo72cp7 FOREIGN KEY (id) REFERENCES idm_notification(id);

ALTER TABLE ONLY idm_role_aud
    ADD CONSTRAINT fk_8eswj46rfgichiptsbm9k2qx7 FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE ONLY idm_notification
    ADD CONSTRAINT fk_8gtuvvraara46uuw7y40ddm2f FOREIGN KEY (sender_id) REFERENCES idm_identity(id);

ALTER TABLE ONLY idm_identity_aud
    ADD CONSTRAINT fk_99gffdyiwsawhuhf5onwvcgyw FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE ONLY idm_identity_working_position
    ADD CONSTRAINT fk_bokg2ecvedrjayicxk5uapsm3 FOREIGN KEY (manager_id) REFERENCES idm_identity(id);

ALTER TABLE ONLY idm_role_composition
    ADD CONSTRAINT fk_bokxu1laccj6t8f4ddduy3ud8 FOREIGN KEY (superior_id) REFERENCES idm_role(id);

ALTER TABLE ONLY idm_identity_role_aud
    ADD CONSTRAINT fk_cwmrwmamrtr8beehnmevcptfh FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE ONLY idm_identity_working_position_aud
    ADD CONSTRAINT fk_e9uky70y1a2uaj2y35wg2wi17 FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE ONLY idm_notification_recipient
    ADD CONSTRAINT fk_esgulluhncor8dagkd2e71k9f FOREIGN KEY (identity_recipient_id) REFERENCES idm_identity(id);

ALTER TABLE ONLY idm_identity_role
    ADD CONSTRAINT fk_grl8dgoxq29agvhwx9ycvq99y FOREIGN KEY (identity_id) REFERENCES idm_identity(id) ON DELETE CASCADE;

ALTER TABLE ONLY idm_identity_role
    ADD CONSTRAINT fk_hh6a5m1l5rkj52mrps4w1a4ti FOREIGN KEY (role_id) REFERENCES idm_role(id);

ALTER TABLE ONLY idm_role_authority_aud
    ADD CONSTRAINT fk_ifgd46riufjsxrbib60lyax6 FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE ONLY idm_role_composition_aud
    ADD CONSTRAINT fk_jealjvqtofaghhmlv8cmlsvev FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE ONLY idm_notification
    ADD CONSTRAINT fk_md4u8k85gjsm7t853ro49474i FOREIGN KEY (parent_notification_id) REFERENCES idm_notification(id);

ALTER TABLE ONLY idm_email_log
    ADD CONSTRAINT fk_mhyevbh5gbrwiw2geq69empko FOREIGN KEY (id) REFERENCES idm_notification(id);

ALTER TABLE ONLY idm_identity_working_position
    ADD CONSTRAINT fk_objykl2b6pnho13ao7bnppewa FOREIGN KEY (organization_id) REFERENCES idm_organization(id);

ALTER TABLE ONLY idm_role_authority
    ADD CONSTRAINT fk_p5jsvce9inw4u2wsji6k13r3q FOREIGN KEY (role_id) REFERENCES idm_role(id) ON DELETE CASCADE;

ALTER TABLE ONLY idm_organization_aud
    ADD CONSTRAINT fk_pdll1wf2b1e1tg2c3nr8rvdnf FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE ONLY idm_console_log
    ADD CONSTRAINT fk_qjgjpxnrb90esl9u8y3mixxif FOREIGN KEY (id) REFERENCES idm_notification(id);

ALTER TABLE ONLY idm_notification_recipient
    ADD CONSTRAINT fk_svipm8scpjnuy2keri4u1whf1 FOREIGN KEY (notification_id) REFERENCES idm_notification(id);

ALTER TABLE ONLY idm_identity_working_position
    ADD CONSTRAINT fk_tg0elpmq774jvn1dihfht4021 FOREIGN KEY (identity_id) REFERENCES idm_identity(id) ON DELETE CASCADE;
    
    
CREATE SEQUENCE hibernate_sequence
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 56
  CACHE 1;