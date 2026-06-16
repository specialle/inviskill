Seed Protection for Paper & Folia 1.21.4+

Most anti-seed plugins just block /seed. But nobody who actually cracks seeds uses /seed.
They grab the hashed seed from packets, triangulate strongholds with ender eyes, or reverse-engineer structure positions.
Antiseedcracker blocks all of that without affecting gameplay.

Commands

    /asc status — shows runtime info and protection status
    /asc seed [world] — shows real and fake seed (admin only)
    /asc scramble-seeds — randomizes structure seeds in spigot.yml
    /asc reload — reloads the config
    /asc info — version info


Permissions

    antiseedcracker.admin (default: OP) — access to admin commands
    antiseedcracker.alerts (default: OP) — receive vulnerability alerts on join
    antiseedcracker.bypass (default: false) — bypass all protection
