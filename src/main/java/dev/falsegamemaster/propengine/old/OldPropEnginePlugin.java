package dev.falsegamemaster.propengine.old;

@Deprecated
public class OldPropEnginePlugin {
//
//    public static Logger LOGGER;
//    public final PropRegistrar REGISTRAR = new PropRegistrar();
//
//    private final Map<UUID, PropEnginePlugin.VehicleInstance> vehicles = new HashMap<>();
//
//    @Override
//    public void onEnable() {
//
//        LOGGER = getLogger();
//
//        getServer().getPluginManager().registerEvents(this, this);
//
//        getCommand("spawnfighter").setExecutor((sender, command, label, args) -> {
//            if (!(sender instanceof Player player)) {
//                sender.sendMessage("Players only.");
//                return true;
//            }
//
//            spawnVehicle(player);
//            return true;
//        });
//
//        getCommand("leavefighter").setExecutor((sender, command, label, args) -> {
//            if (!(sender instanceof Player player)) {
//                sender.sendMessage("Players only.");
//                return true;
//            }
//
//            despawnVehicle(player);
//            return true;
//        });
//    }
//
//    private void spawnVehicle(Player player) {
//        if (vehicles.containsKey(player.getUniqueId())) {
//            player.sendMessage("You already have a vehicle.");
//            return;
//        }
//
//        Location spawn = player.getLocation();
//
//        ArmorStand root = spawn.getWorld().spawn(spawn, ArmorStand.class, stand -> {
//            stand.setVisible(false);
//            stand.setGravity(false);
//            stand.setMarker(true);
//            stand.setInvulnerable(true);
//            stand.setSilent(true);
//            stand.setPersistent(false);
//        });
//
//        ItemDisplay body = spawn.getWorld().spawn(spawn, ItemDisplay.class, display -> {
//            display.setItemStack(new ItemStack(Material.GOLD_NUGGET)); // placeholder
//            display.setTeleportDuration(1);
//            display.setInterpolationDuration(1);
//
//            // Make it larger so it's easier to see as a placeholder craft body.
//            Transformation tf = new Transformation(
//                    new Vector3f(0f, 0f, 0f),
//                    new AxisAngle4f(0f, 0f, 1f, 0f),
//                    new Vector3f(16.0f, 16.0f, 16.0f),
//                    new AxisAngle4f(0f, 0f, 1f, 0f)
//            );
//            display.setTransformation(tf);
//        });
//
//        root.addPassenger(player);
//
//        PropEnginePlugin.VehicleInstance vehicle = new PropEnginePlugin.VehicleInstance(player, root, body);
//        vehicle.task = startTickTask(vehicle);
//
//        vehicles.put(player.getUniqueId(), vehicle);
//        player.sendMessage("Vehicle spawned.");
//    }
//
//    private void despawnVehicle(Player player) {
//        PropEnginePlugin.VehicleInstance vehicle = vehicles.remove(player.getUniqueId());
//        if (vehicle == null) {
//            player.sendMessage("No vehicle to remove.");
//            return;
//        }
//
//        if (vehicle.task != null) {
//            vehicle.task.cancel();
//        }
//
//        vehicle.root.removePassenger(player);
//        vehicle.body.remove();
//        vehicle.root.remove();
//
//        player.sendMessage("Vehicle removed.");
//    }
//
//    private BukkitTask startTickTask(PropEnginePlugin.VehicleInstance vehicle) {
//        return Bukkit.getScheduler().runTaskTimer(this, () -> {
//            Player pilot = vehicle.pilot;
//            if (!pilot.isOnline() || pilot.isDead() || !vehicle.root.isValid() || !vehicle.body.isValid() || pilot.getVehicle() != vehicle.root) {
//                despawnVehicle(pilot);
//                return;
//            }
//            Location pilotLook = pilot.getLocation();
//            float yaw = pilotLook.getYaw();
//            float pitch = pilotLook.getPitch();
//            Location rootLoc = vehicle.root.getLocation();
//            rootLoc.setYaw(yaw);
//            rootLoc.setPitch(pitch);
//            Vector forwardFlat = pilotLook.getDirection().clone();
//            forwardFlat.setY(0);
//            forwardFlat = forwardFlat.lengthSquared() < 1.0e-6 ? new Vector(0, 0, 1) : forwardFlat.normalize();
//            Vector rightFlat = new Vector(-forwardFlat.getZ(), 0, forwardFlat.getX()).normalize();
//            Vector inputVelocity = pilot.getVelocity().clone();
//            inputVelocity.setY(0);
//            double forwardAmount = inputVelocity.dot(forwardFlat);
//            double strafeAmount = inputVelocity.dot(rightFlat);
//            double forwardDeadzone = 0.01, strafeDeadzone = 0.01;
//            boolean forwardPressed = forwardAmount > forwardDeadzone, backwardPressed = forwardAmount < -forwardDeadzone, leftPressed = strafeAmount < -strafeDeadzone, rightPressed = strafeAmount > strafeDeadzone;
//            double speed = 0.35;
//            Vector lookDirection = pilotLook.getDirection().clone().normalize();
//            Vector movement = new Vector();
//            if (forwardPressed) movement.add(lookDirection.clone().multiply(speed));
//            else if (backwardPressed) movement.add(lookDirection.clone().multiply(-speed));
//            Location next = rootLoc.clone().add(movement);
//            next.setYaw(yaw);
//            next.setPitch(pitch);
//            vehicle.root.teleport(next, TeleportFlag.EntityState.RETAIN_PASSENGERS);
//            float rollStep = 4.0f, rollReturn = 2.0f, maxRoll = 45.0f;
//            if (leftPressed) vehicle.rollDegrees -= rollStep;
//            else if (rightPressed) vehicle.rollDegrees += rollStep;
//            if (!leftPressed && !rightPressed) {
//                if (vehicle.rollDegrees > 0.0f) vehicle.rollDegrees = Math.max(0.0f, vehicle.rollDegrees - rollReturn);
//                else if (vehicle.rollDegrees < 0.0f) vehicle.rollDegrees = Math.min(0.0f, vehicle.rollDegrees + rollReturn);
//            }
//            vehicle.rollDegrees = Math.max(-maxRoll, Math.min(maxRoll, vehicle.rollDegrees));
////            float rollStep = 4.0f;
////            if (leftPressed) vehicle.rollDegrees -= rollStep;
////            else if (rightPressed) vehicle.rollDegrees += rollStep;
//            Location bodyLoc = next.clone().add(0, -0.2, 0);
//            bodyLoc.setYaw(0f);
//            bodyLoc.setPitch(0f);
//            vehicle.body.teleport(bodyLoc);
//            Quaternionf rotation = new Quaternionf()
//                    .rotateY((float) Math.toRadians(-yaw))
//                    .rotateX((float) Math.toRadians(pitch))
//                    .rotateZ((float) Math.toRadians(vehicle.rollDegrees));
//            Transformation transform = new Transformation(
//                    new Vector3f(0f, 0f, 0f),
//                    rotation,
//                    new Vector3f(16.0f, 16.0f, 16.0f),
//                    new Quaternionf()
//            );
//            vehicle.body.setTransformation(transform);
//        }, 1L, 1L);
//    }
//
////    private BukkitTask startTickTask(VehicleInstance vehicle) {
////        return Bukkit.getScheduler().runTaskTimer(this, () -> {
////            Player pilot = vehicle.pilot;
////
////            if (!pilot.isOnline() || pilot.isDead() || !vehicle.root.isValid() || !vehicle.body.isValid()) {
////                despawnVehicle(pilot);
////                return;
////            }
////
////            if (pilot.getVehicle() != vehicle.root) {
////                despawnVehicle(pilot);
////                return;
////            }
////
////            Location pilotLook = pilot.getLocation();
////            float yaw = pilotLook.getYaw();
////            float pitch = pilotLook.getPitch();
////
////            Location rootLoc = vehicle.root.getLocation();
////            rootLoc.setYaw(yaw);
////            rootLoc.setPitch(pitch);
////
////            // Horizontal facing vectors based on where the player is looking.
////            Vector forwardFlat = pilotLook.getDirection().clone();
////            forwardFlat.setY(0);
////
////            if (forwardFlat.lengthSquared() < 1.0e-6) {
////                forwardFlat = new Vector(0, 0, 1);
////            } else {
////                forwardFlat.normalize();
////            }
////
////            // Right vector on the horizontal plane.
////            Vector rightFlat = new Vector(-forwardFlat.getZ(), 0, forwardFlat.getX()).normalize();
////
////            // Use the player's current velocity to infer input direction.
////            Vector inputVelocity = pilot.getVelocity().clone();
////            inputVelocity.setY(0);
////
////            double forwardAmount = inputVelocity.dot(forwardFlat);
////            double strafeAmount = inputVelocity.dot(rightFlat);
////
////            // Deadzones to avoid drift/noise.
////            double forwardDeadzone = 0.0001;
////            double strafeDeadzone = 0.0001;
////
////            boolean forwardPressed = forwardAmount > forwardDeadzone;
////            boolean backwardPressed = forwardAmount < -forwardDeadzone;
////            boolean leftPressed = strafeAmount < -strafeDeadzone;
////            boolean rightPressed = strafeAmount > strafeDeadzone;
////
////            // Movement in the full look direction, so looking up/down changes flight path.
////            double speed = 0.35;
////            Vector lookDirection = pilotLook.getDirection().clone().normalize();
////            Vector movement = new Vector();
////
////            if (forwardPressed) {
////                movement.add(lookDirection.clone().multiply(speed));
////            }
////            if (backwardPressed) {
////                movement.add(lookDirection.clone().multiply(-speed));
////            }
////
////            // Move the root only once.
////            Location next = rootLoc.clone().add(movement);
////            next.setYaw(yaw);
////            next.setPitch(pitch);
////            vehicle.root.teleport(next, TeleportFlag.EntityState.RETAIN_PASSENGERS);
////
////            // Roll input from inferred strafe.
////            float rollStep = 4.0f;
////            float rollReturn = 2.0f;
////            float maxRoll = 45.0f;
////
////            getLogger().info("vehicle.rollDegrees: " + vehicle.rollDegrees);
////
////            if (leftPressed) {
////                vehicle.rollDegrees -= rollStep;
////            }
////            if (rightPressed) {
////                vehicle.rollDegrees += rollStep;
////            }
////
////            // Auto-level when no roll input is inferred.
////            if (!leftPressed && !rightPressed) {
////                if (vehicle.rollDegrees > 0.0f) {
////                    vehicle.rollDegrees = Math.max(0.0f, vehicle.rollDegrees - rollReturn);
////                } else if (vehicle.rollDegrees < 0.0f) {
////                    vehicle.rollDegrees = Math.min(0.0f, vehicle.rollDegrees + rollReturn);
////                }
////            }
////
////            vehicle.rollDegrees = Math.max(-maxRoll, Math.min(maxRoll, vehicle.rollDegrees));
////
////            // Keep body at root position.
////            Location bodyLoc = next.clone().add(0, -0.2, 0);
//////            bodyLoc.setYaw(yaw);
//////            bodyLoc.setPitch(pitch);
////            vehicle.body.teleport(bodyLoc);
////
////            // Apply model rotation: yaw/pitch follow camera, roll follows inferred A/D.
////            Quaternionf rotation = new Quaternionf()
////                    .rotateY((float) Math.toRadians(-yaw))
////                    .rotateX((float) Math.toRadians(pitch))
////                    .rotateAxis((float) Math.toRadians(vehicle.rollDegrees), (float) lookDirection.getX(), (float) lookDirection.getY(), (float) lookDirection.getZ());
//////            Quaternionf rotation = new Quaternionf()
//////                    .rotateY((float) Math.toRadians(-yaw))
//////                    .rotateX((float) Math.toRadians(pitch))
//////                    .rotateZ((float) Math.toRadians(vehicle.rollDegrees))
//////                    .rotateY((float) Math.toRadians(90)); // example correction
//////            Transformation transform = vehicle.body.getTransformation();
//////            Quaternionf leftRotation = transform.getLeftRotation();
//////            leftRotation.rotateAxis(vehicle.rollDegrees, (float) lookDirection.getX(), (float) lookDirection.getY(), (float) lookDirection.getZ());
//////            transform = new Transformation(transform.getTranslation(), leftRotation, transform.getScale(), transform.getRightRotation());
////
////            Transformation transform = new Transformation(
////                    new Vector3f(0f, 0f, 0f),
////                    rotation,
////                    new Vector3f(2.5f, 1.0f, 2.5f),
////                    new Quaternionf()
////            );
////
////            vehicle.body.setTransformation(transform);
////
////        }, 1L, 1L);
////    }
//
////    private BukkitTask startTickTask(VehicleInstance vehicle) {
////        return Bukkit.getScheduler().runTaskTimer(this, () -> {
////            Player pilot = vehicle.pilot;
////
////            if (!pilot.isOnline() || pilot.isDead() || !vehicle.root.isValid() || !vehicle.body.isValid()) {
////                despawnVehicle(pilot);
////                return;
////            }
////
////            // If player somehow dismounted, stop.
////            if (pilot.getVehicle() != vehicle.root) {
////                despawnVehicle(pilot);
////                return;
////            }
////
////            Location rootLoc = vehicle.root.getLocation();
////
////            // Face where the player is looking.
////            rootLoc.setYaw(pilot.getLocation().getYaw());
////            rootLoc.setPitch(pilot.getLocation().getPitch());
////
////            // Very basic "W to move forward" approximation:
////            // if player is sprinting, go faster.
////            double speed = pilot.isSprinting() ? 0.9 : 0.45;
////
////            Vector forward = pilot.getLocation().getDirection().normalize().multiply(speed);
////
////            // Optional: flatten pitch a bit so it is easier to control.
////            // Comment this out if you want full 3D flight.
////            // forward.setY(0).normalize().multiply(speed);
////
////            Location next = rootLoc.clone().add(forward);
////
////            // Move the root.
////            vehicle.root.teleport(next, TeleportFlag.EntityState.RETAIN_PASSENGERS);
//    ////            getLogger().info("teleport returned: " + moved);
//    ////            getLogger().info("expected: " + next);
//    ////            getLogger().info("actual:   " + vehicle.root.getLocation());
//    ////            getLogger().info("");
////
////            // Put the body at the root.
////            Location bodyLoc = next.clone().add(0, -0.2, 0);
////            bodyLoc.setYaw(next.getYaw());
////            bodyLoc.setPitch(next.getPitch());
////            vehicle.body.teleport(bodyLoc);
////
////        }, 1L, 1L);
////    }
//
//    private static final class VehicleInstance {
//        private final Player pilot;
//        private final ArmorStand root;
//        private final ItemDisplay body;
//        private BukkitTask task;
//        private float rollDegrees = 0.0f;
//
//        private VehicleInstance(Player pilot, ArmorStand root, ItemDisplay body) {
//            this.pilot = pilot;
//            this.root = root;
//            this.body = body;
//        }
//    }
//
////    @Override
////    public void onEnable() {
////        // Plugin startup logic
////
////    }
////
////    @Override
////    public void onDisable() {
////        // Plugin shutdown logic
////    }
}
