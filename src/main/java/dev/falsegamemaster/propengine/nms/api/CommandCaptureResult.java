package dev.falsegamemaster.propengine.nms.api;

import java.util.List;

public record CommandCaptureResult(boolean success, int result, List<String> messages) {
}
