package dev.falsegamemaster.propengine.util;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform {

    public static Transform identity() {
        return new Transform(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
    }

    public static Transform rotation(Quaternionf rotation) {
        return new Transform(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), rotation, new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
    }

    public static Transform of(AdvancedLocation location) {
        return new Builder(location).build();
    }

    private final Vector3f translation, pivot, scale;
    private final Quaternionf preScaleRotation, postScaleRotation;

    public Transform(Vector3f translation, Vector3f pivot, Quaternionf preScaleRotation, Vector3f scale, Quaternionf postScaleRotation) {
        this.translation = new Vector3f(translation);
        this.pivot = new Vector3f(pivot);
        this.preScaleRotation = new Quaternionf(preScaleRotation);
        this.scale = new Vector3f(scale);
        this.postScaleRotation = new Quaternionf(postScaleRotation);
    }

    public Vector3f getTranslation() {
        return new Vector3f(translation);
    }

    public Vector3f getPivot() {
        return new Vector3f(pivot);
    }

    public Quaternionf getPreScaleRotation() {
        return new Quaternionf(preScaleRotation);
    }

    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    public Quaternionf getPostScaleRotation() {
        return new Quaternionf(postScaleRotation);
    }

    /**
     * Applies this transform to a point in local space.
     */
    public Vector3f transformPoint(Vector3f point) {
        Vector3f result = new Vector3f(point);
        result.add(translation);
        result.sub(pivot); // Move point into pivot-relative space
        result.rotate(preScaleRotation); // Apply pre-scale rotation
        result.mul(scale); // Apply scale
        result.rotate(postScaleRotation); // Apply post-scale rotation
        // Move back out of pivot-relative space, then apply translation
        result.add(pivot);
        return result;
    }

    /**
     * Applies this transform to a direction/orientation basis.
     * For now, scale is intentionally ignored for orientation composition.
     */
    public Quaternionf transformRotation(Quaternionf rotation) {
        return new Quaternionf(postScaleRotation).mul(preScaleRotation).mul(rotation);
    }

    /**
     * Compose parent ∘ child:
     * first apply child, then apply parent.
     */
    public static Transform compose(Transform parent, Transform child) {
        Vector3f composedTranslation = parent.transformPoint(child.transformPoint(new Vector3f()));
        Quaternionf composedPreScaleRotation = new Quaternionf(parent.getPreScaleRotation()).mul(child.getPreScaleRotation());
        Quaternionf composedPostScaleRotation = new Quaternionf(parent.getPostScaleRotation()).mul(child.getPostScaleRotation());
        Vector3f composedScale = new Vector3f(parent.getScale()).mul(child.getScale());
        return new Transform(composedTranslation, new Vector3f(), composedPreScaleRotation, composedScale, composedPostScaleRotation);
    }

    /**
     * Convenience instance form: this ∘ child
     */
    public Transform compose(Transform child) {
        return compose(this, child);
    }

    /**
     * Convert this logical transform into the final Bukkit display transformation.
     */
    public Transformation bake() {
        Vector3f bakedTranslation = bakeTranslation(translation, pivot, preScaleRotation, scale, postScaleRotation);
        return new Transformation(bakedTranslation, new Quaternionf(preScaleRotation), new Vector3f(scale), new Quaternionf(postScaleRotation));
    }

    /**
     * Converts pivoted logical translation into the actual display translation.
     */
    private static Vector3f bakeTranslation(Vector3f translation, Vector3f pivot, Quaternionf preScaleRotation, Vector3f scale, Quaternionf postScaleRotation) {
        Vector3f pivotOffset = new Vector3f(pivot);
        pivotOffset.rotate(preScaleRotation);
        pivotOffset.mul(scale);
        pivotOffset.rotate(postScaleRotation);
        return new Vector3f(translation).add(pivot).sub(pivotOffset);
    }

    public static class Builder {
        private Vector3f translation = new Vector3f(0.0f, 0.0f, 0.0f), pivot = new Vector3f(0.0f, 0.0f, 0.0f), scale = new Vector3f(1.0f, 1.0f, 1.0f);
        private Quaternionf preScaleRotation = new Quaternionf(), postScaleRotation = new Quaternionf();

        public Builder() {}

        public Builder(AdvancedLocation location) {
            float yawRadians = (float) Math.toRadians(-location.getYaw());
            float pitchRadians = (float) Math.toRadians(location.getPitch());
            float rollRadians = (float) Math.toRadians(location.getRoll());
            this.preScaleRotation = new Quaternionf().rotateY(yawRadians).rotateX(pitchRadians).rotateZ(rollRadians);
        }

        public Builder translation(Vector3f translation) {
            this.translation = translation;
            return this;
        }

        public Builder pivot(Vector3f pivot) {
            this.pivot = pivot;
            return this;
        }

        public Builder preScaleRotation(Quaternionf preScaleRotation) {
            this.preScaleRotation = preScaleRotation;
            return this;
        }

        public Builder scale(Vector3f scale) {
            this.scale = scale;
            return this;
        }

        public Builder postScaleRotation(Quaternionf postScaleRotation) {
            this.postScaleRotation = postScaleRotation;
            return this;
        }

        public Transform build() {
            return new Transform(translation, pivot, preScaleRotation, scale, postScaleRotation);
        }
    }

    @Override
    public String toString() {
        return "Transform{" + "translation=" + translation + ", pivot=" + pivot + ", scale=" + scale + ", preScaleRotation=" + preScaleRotation + ", postScaleRotation=" + postScaleRotation + '}';
    }
}
