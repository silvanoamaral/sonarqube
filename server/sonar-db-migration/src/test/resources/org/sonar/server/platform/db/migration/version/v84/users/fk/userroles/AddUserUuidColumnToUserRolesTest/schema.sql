CREATE TABLE "USER_ROLES"(
    "ORGANIZATION_UUID" VARCHAR(40) NOT NULL,
    "USER_ID" INTEGER,
    "ROLE" VARCHAR(64) NOT NULL,
    "COMPONENT_UUID" VARCHAR(40),
    "UUID" VARCHAR(40) NOT NULL
);
ALTER TABLE "USER_ROLES" ADD CONSTRAINT "PK_USER_ROLES" PRIMARY KEY("UUID");
CREATE INDEX "USER_ROLES_USER" ON "USER_ROLES"("USER_ID");
CREATE INDEX "USER_ROLES_COMPONENT_UUID" ON "USER_ROLES"("COMPONENT_UUID");
