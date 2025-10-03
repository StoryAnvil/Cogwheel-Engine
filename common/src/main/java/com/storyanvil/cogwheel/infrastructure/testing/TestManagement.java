/*
 *
 * StoryAnvil Cogwheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.infrastructure.testing;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.config.CogwheelClientConfig;
import com.storyanvil.cogwheel.infrastructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrastructure.cog.CogTestCallback;
import com.storyanvil.cogwheel.infrastructure.env.TestEnvironment;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.script.ScriptLine;
import com.storyanvil.cogwheel.infrastructure.script.StreamExecutionScript;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TestManagement { //TODO: Perform testing in normal minecraft environment (without IDE, for example in curseforge launcher)
    private static final Logger log = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/TESTS");
    private TestManagement() {}

    private final ArrayList<Result> results = new ArrayList<>();
    private boolean autoOnly;
    private boolean performanceTesting = false;

    public static void startTesting(boolean autoOnly) {
        String uuid = UUID.randomUUID().toString();
        Thread thread = new Thread(() -> startTestingInternal(autoOnly, false, uuid), "storyanvil-test-worker-" + uuid);
        thread.start();
    }
    public static void startTestingPerformance() {
        String uuid = UUID.randomUUID().toString();
        Thread thread = new Thread(() -> startTestingInternal(true, true, uuid), "storyanvil-test-worker-" + uuid);
        thread.start();
    }
    private static void sendTestingMessage(Text msg) {
        try {
            for (ServerPlayerEntity plr : CogwheelHooks.getOverworldServer().getServer().getPlayerManager().getPlayerList()) {
                plr.sendMessage(msg);
            }
        } catch (NullPointerException ignored) {}
    }
    private static void startTestingInternal(boolean autoOnly, boolean performanceTesting, String id) {
        File testingFolder = new File(CogwheelHooks.getConfigFolder(), "cog/tests");
        if (!CogwheelClientConfig.getTestsDirectory().isEmpty()) {
            File copyTests = new File(CogwheelClientConfig.getTestsDirectory());
            StoryUtils.deleteDirectory(testingFolder);
            //noinspection ResultOfMethodCallIgnored
            testingFolder.mkdir();
            try {
                FileUtils.copyDirectory(copyTests, testingFolder);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy testing folder", e);
            }
            sendTestingMessage(Text.literal("[CogwheelEngine] Tests copied from " + CogwheelClientConfig.getTestsDirectory()).formatted(Formatting.GRAY));
        }
        if (!testingFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            testingFolder.mkdirs();
            return;
        }
        TestManagement management = new TestManagement();
        management.autoOnly = autoOnly;
        management.performanceTesting = performanceTesting;
        sendTestingMessage(Text.literal("[CogwheelEngine] Testing started").formatted(Formatting.GRAY));
        StoryUtils.discoverDirectory(testingFolder, management::runTest);
        JsonArray obj = new JsonArray();
        int fails = 0;
        for (Result testResult : management.results) {
            JsonObject test = new JsonObject();
            test.addProperty("name", testResult.testName);
            test.addProperty("comment", testResult.comment);
            test.addProperty("status", testResult.status.name());
            test.addProperty("time", testResult.timeSpend + "ms");
            if (testResult.fail != null) {
                JsonArray trace = new JsonArray();
                createTrace(trace, testResult.fail, "Exception in test ");
                test.add("trace", trace);
            }
            if (testResult.getData() != null) {
                test.add("data", testResult.getData());
            }
            if (testResult.status == Status.TEST_FAILED || testResult.status == Status.MANAGER_FAILED) {
                fails++;
            }
            obj.add(test);
        }
        JsonObject report = new JsonObject();
        report.addProperty("failedTests", fails);
        report.addProperty("date", new Date().toString());
        report.addProperty("id", id);
        JsonObject env = new JsonObject();
        env.addProperty("platform", CogwheelHooks.getPlatform().name());
        env.addProperty("cogwheelVersion", CogwheelHooks.getVersion());
        try {
            env.addProperty("jre-version", Runtime.version().toString());
            env.addProperty("jre-version", System.getProperty("java.version"));
            env.addProperty("jre-vendor", System.getProperty("java.vendor"));
            env.addProperty("jvm-spec-version", System.getProperty("java.vm.specification.version"));
            env.addProperty("os-name", System.getProperty("os.name"));
            env.addProperty("os-arch", System.getProperty("os.arch"));
            env.addProperty("os-version", System.getProperty("os.version"));
            if (CogwheelClientConfig.isRunningInIDE()) {
                JsonObject configs = new JsonObject();
                configs.add("client", CogwheelClientConfig.getConfig());
                configs.add("default", CogwheelConfig.getConfig());
                env.add("configs", configs);
            } else {
                env.addProperty("configs", "IDE mode is disabled. Can not get configs");
            }
        } catch (Exception e) {
            JsonArray trace = new JsonArray();
            createTrace(trace, e, "Failed to get env data. Exception ");
            env.add("fail", trace);
            log.error("Failed to get env", e);
        }
        report.add("environment", env);
        report.add("results", obj);

        if (fails == 0) {
            sendTestingMessage(Text.literal("[CogwheelEngine] Testing finished. ").formatted(Formatting.GRAY)
                    .append(Text.literal("All tests were successful!").formatted(Formatting.GREEN)));
        } else {
            sendTestingMessage(Text.literal("[CogwheelEngine] Testing finished. ").formatted(Formatting.GRAY)
                    .append(Text.literal(fails + " tests failed!").formatted(Formatting.RED)));
        }
        File resultsFile = new File(testingFolder, "results.json");
        try (FileWriter fw = new FileWriter(resultsFile)) {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setStrictness(Strictness.LENIENT);
            jsonWriter.setIndent("    ");
            Streams.write(report, jsonWriter);
            fw.write(stringWriter.toString());
            fw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!CogwheelClientConfig.getTestsDirectory().isEmpty()) {
            try {
                FileUtils.copyFile(resultsFile, new File(CogwheelClientConfig.getTestsDirectory(), "results.json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void createTrace(@NotNull JsonArray array, @Nullable Throwable t, @NotNull String msg) {
        if (t == null) return;
        array.add(msg + t.getClass().getCanonicalName() + ": " + t.getMessage() + (t.getLocalizedMessage() == null || t.getLocalizedMessage().equals(t.getMessage()) ? "[NO LOCALIZED MSG]" : " [" + t.getLocalizedMessage() + "]"));
        for (StackTraceElement e : t.getStackTrace()) {
            array.add(" at " + e.toString());
        }
        for (Throwable suppressed : t.getSuppressed()) {
            JsonArray supr = new JsonArray();
            createTrace(supr, suppressed, "Suppressed ");
            array.add(supr);
        }
        if (t.getCause() != null) {
            JsonArray cause = new JsonArray();
            createTrace(cause, t.getCause(), "Caused by ");
            array.add(cause);
        }
    }

    public static final int PERFORMANCE_ITERATIONS = 10;
    public void runTest(File file, String path) {
        if (file.getName().equals("results.json")) return;
        if (!file.getName().endsWith(".json")) return;
        long timeStarted = System.currentTimeMillis();
        Result result = new Result(path.substring(0, path.lastIndexOf('.')));
        results.add(result);
        try (FileReader fr = new FileReader(file)) {
            JsonObject obj = JsonParser.parseReader(fr).getAsJsonObject();
            if (obj.has("comment")) {
                result.comment = obj.get("comment").getAsString();
            }
            if (obj.has("auto") && !obj.get("auto").getAsBoolean() && autoOnly) {
                result.setStatus(Status.SKIPPED);
                result.setFail(new WrapperException("Test is marked as NON-AUTO, but current testing context only allows AUTO tests"));
                sendTestingMessage(Text.literal("[CogwheelEngine] " + result.testName + " -> ").formatted(Formatting.GRAY)
                        .append(Text.literal(result.status.name()).formatted(result.status.formatting)));
                return;
            }
            boolean isStressTest = obj.has("stress") && obj.getAsJsonPrimitive("stress").getAsBoolean();
            if (isStressTest) {
                if (!performanceTesting) {
                    result.setStatus(Status.SKIPPED);
                    result.setFail(new WrapperException("Test is marked as STRESS-TEST, but current testing context does not run STRESS-TESTS"));
                    sendTestingMessage(Text.literal("[CogwheelEngine] " + result.testName + " -> ").formatted(Formatting.GRAY)
                            .append(Text.literal(result.status.name()).formatted(result.status.formatting)));
                    return;
                }
                TestType type = TestType.valueOf(obj.get("type").getAsString());
                result.setStatus(Status.OK);
                ArrayList<Profiler> profilers = new ArrayList<>(PERFORMANCE_ITERATIONS);
                for (int i = 0; i <= PERFORMANCE_ITERATIONS; i++) {
                    Result iterationResult = new Result(result.getTestName() + '#' + i);
                    iterationResult.profiler = new Profiler(System.currentTimeMillis());
                    try {
                        type.handler.handle(iterationResult, obj, this);
                    } catch (Throwable t) {
                        result.failWith(new WrapperException("At performance testing iteration #" + i, t));
                        log.error("Test {} failed with exception", result.testName, result.fail);
                        break;
                    }
                    if (iterationResult.fail != null) {
                        iterationResult.status = Status.TEST_FAILED;
                    }
                    iterationResult.profiler.finishState("Finish");
                    profilers.add(iterationResult.profiler);
                }
                try {
                    int spacing = 0;
                    for (Bi<Integer, String> state : profilers.getFirst().timings) {
                        spacing = Math.max(state.getB().length(), spacing);
                    }
                    JsonArray timings = new JsonArray();
                    for (Bi<Integer, String> state : profilers.getFirst().timings) {
                        int avg = 0;
                        int avgAmount = 0;
                        StringBuilder sb = new StringBuilder();
                        sb.append(state.getB()).append(" ".repeat(spacing - state.getB().length()));
                        sb.append(" | ");

                        profs:
                        for (Profiler profiler : profilers) {
                            Bi<Integer, String> pState = null;
                            int pStateIndex = 0;
                            while (pState == null && pStateIndex < profiler.timings.size()) {
                                if (profiler.timings.get(pStateIndex).getB().equals(state.getB())) {
                                    pState = profiler.timings.get(pStateIndex);
                                }
                                pStateIndex++;
                            }
                            if (pState == null) {
                                sb.append(" ---  | ");
                                continue profs;
                            }
                            String time = String.valueOf(pState.getA());
                            avgAmount++;
                            avg += pState.getA();
                            if (time.length() > 5) {
                                sb.append("OVERF | ");
                            } else if (time.length() == 5) {
                                sb.append(time).append(" | ");
                            } else {
                                sb.append(" ".repeat(5 - time.length())).append(time).append(" | ");
                            }
                        }
                        String time = String.valueOf(avg / avgAmount);
                        if (time.length() > 5) {
                            sb.append("OVERF | ");
                        } else if (time.length() == 5) {
                            sb.append(time).append(" | ");
                        } else {
                            sb.append(" ".repeat(5 - time.length())).append(time).append(" | ");
                        }
                        timings.add(sb.toString());
                    }
                    result.addData("profilers", timings);
                    int avg = 0;
                    for (Profiler profiler : profilers) {
                        for (Bi<Integer, String> timing : profiler.timings) {
                            avg += timing.getA();
                        }
                    }
                    avg /= profilers.size();
                    result.addData("avgTime", new JsonPrimitive(avg));
                } catch (Exception e) {
                    result.failWith(new WrapperException("Failed to collect statistics", e));
                }
            } else {
                if (performanceTesting) {
                    result.setStatus(Status.SKIPPED);
                    result.setFail(new WrapperException("Test is NOT marked as STRESS-TEST, but current testing context does not run STRESS-TESTS"));
                    sendTestingMessage(Text.literal("[CogwheelEngine] " + result.testName + " -> ").formatted(Formatting.GRAY)
                            .append(Text.literal(result.status.name()).formatted(result.status.formatting)));
                    return;
                }
                TestType type = TestType.valueOf(obj.get("type").getAsString());
                try {
                    type.handler.handle(result, obj, this);
                } catch (Throwable e) {
                    result.failWith(e);
                    log.error("Test {} failed with exception", result.testName, result.fail);
                }
                if (result.fail != null) {
                    result.status = Status.TEST_FAILED;
                }
            }
        } catch (IOException | JsonIOException | JsonSyntaxException | IllegalStateException | IllegalArgumentException e) {
            result.failWith(e);
            result.setStatus(Status.MANAGER_FAILED);
        }
        result.timeSpend = System.currentTimeMillis() - timeStarted;
        sendTestingMessage(Text.literal("[CogwheelEngine] " + result.testName + " -> ").formatted(Formatting.GRAY)
                .append(Text.literal(result.status.name()).formatted(result.status.formatting)));
    }

    public static class Result {
        private final String testName;
        private Status status = Status.IN_PROGRESS;
        private Throwable fail = null;
        private long timeSpend = -1;
        public Profiler profiler;
        private JsonObject data = null;
        private String comment = "no comment";

        public void addData(String key, JsonElement data) {
            if (this.data == null) this.data = new JsonObject();
            this.data.add(key, data);
        }

        public JsonObject getData() {
            return data;
        }

        public Result(String testName) {
            this.testName = testName;
        }

        public String getTestName() {
            return testName;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Throwable getFail() {
            return fail;
        }

        public void setFail(Throwable fail) {
            this.fail = fail;
        }

        public void failWith(Throwable fail) {
            if (this.fail == null)
                this.fail = fail;
            else {
                if (this.fail instanceof CompoundException compound) {
                    compound.addUniqueException(fail);
                } else {
                    CompoundException ce = new CompoundException(this.fail);
                    this.fail = ce;
                    ce.addUniqueException(fail);
                }
            }
            this.status = Status.TEST_FAILED;
        }

        public boolean failIf(boolean shouldFail, Supplier<Throwable> failure) throws Throwable {
            if (shouldFail) {
                Throwable t = failure.get();
                failWith(t);
                throw t;
            }
            return false;
        }
        public boolean failIf(boolean shouldFail, String msg) throws Throwable {
            return failIf(shouldFail, () -> new RuntimeException(msg));
        }
        public <T> T failIfNull(T check, Supplier<Throwable> failure) throws Throwable {
            if (check == null) {
                Throwable t = failure.get();
                failWith(t);
                throw t;
            }
            return check;
        }
        public <T> T failIfNull(T check, String msg) throws Throwable {
            return failIfNull(check, () -> new RuntimeException(msg));
        }
        public boolean failIfNot(boolean shouldNotFail, Supplier<Throwable> failure) throws Throwable {
            if (!shouldNotFail) {
                Throwable t = failure.get();
                failWith(t);
                throw t;
            }
            return true;
        }
        public boolean failIfNot(boolean shouldNotFail, String msg) throws Throwable {
            return failIfNot(shouldNotFail, () -> new RuntimeException(msg));
        }
        public <E extends Throwable> void executeSafely(DangerousRunnable<E> r) {
            try {
                r.run();
            } catch (Throwable e) {
                failWith(e);
            }
        }
        public <E extends Throwable> void executeSafely(DangerousRunnable<E> r, String failMsg) {
            try {
                r.run();
            } catch (Throwable e) {
                failWith(new WrapperException(failMsg, e));
            }
        }
    }
    public static class Profiler {
        private long started;
        private final ArrayList<Bi<Integer, String>> timings = new ArrayList<>();

        public Profiler(long started) {
            this.started = started;
        }

        public void finishState(String name) {
            long l = System.currentTimeMillis();
            timings.add(new Bi<>(Math.toIntExact(l - started), name));
            started = l;
        }

//        public JsonArray toJSON() {
//            JsonArray array = new JsonArray();
//            int last = 0;
//            for (Bi<Integer, String> report : timings) {
//                array.add("@%sms - %s (this step took %sms)".formatted(report.getA(), report.getB(), report.getA() - last));
//                last = report.getA();
//            }
//            return array;
//        }
    }
    public static enum Status {
        IN_PROGRESS(Formatting.GOLD), TEST_FAILED(Formatting.RED), MANAGER_FAILED(Formatting.RED), OK(Formatting.GREEN), SKIPPED(Formatting.DARK_GRAY);
        public final Formatting formatting;

        Status(Formatting formatting) {
            this.formatting = formatting;
        }
    }
    public static interface TestTypeHandler {
        void handle(Result result, JsonObject manifest, TestManagement management) throws Exception;
    }
    public static enum TestType {
        OK((result, manifest, management) -> {
            result.setStatus(Status.OK);
        }),
        FAIL((result, manifest, management) -> {
            throw new RuntimeException("Always fails.");
        }),
        SCRIPT((result, manifest, management) -> {
            CountDownLatch threadLock = new CountDownLatch(1);
            TestEnvironment environment = new TestEnvironment();
            CogTestCallback callback = new CogTestCallback(result, management);
            DispatchedScript script = CogScriptDispatcher.dispatchUnsafe(environment.getScript(manifest.getAsJsonPrimitive("scriptName").getAsString()), new ScriptStorage()
                    .append("TEST", callback),  environment, false);
            if (script == null) throw new AssertionError("No script were found!");
            CogwheelExecutor.schedule(() -> {
                script.setOnEnd(threadLock::countDown);
                script.setErrorHandler(scriptException -> {
                    result.failWith(new WrapperException("Script exception got caught", scriptException));
                });
                try {
                    Thread.sleep(2); // Give some time for test-worker to start waiting
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                script.lineDispatcher();
            });
            boolean executed = threadLock.await(5000, TimeUnit.MILLISECONDS);
            if (executed) {
                result.setStatus(Status.OK);

                // Check script assertions here

                script.haltExecution();
                environment.dispose();
            } else {
                script.haltExecution();
                environment.dispose();
                throw new AssertionError("Script execution timeout reached. Script took longer than 5000ms and was terminated");
            }
        }),
        SCRIPT_READER((result, manifest, management) -> {
            TestEnvironment environment = new TestEnvironment();
            DispatchedScript script = CogScriptDispatcher.dispatchUnsafe(environment.getScript(manifest.getAsJsonPrimitive("scriptName").getAsString()), new ScriptStorage(),  environment, false);
            if (script == null) throw new WrapperException("No scripts were found!");
            String entireScript = script.getLinesToExecute().stream().map(ScriptLine::getLine).collect(Collectors.joining("\\n"));
            if (!entireScript.equals(manifest.getAsJsonPrimitive("expectedScript").getAsString())) {
                log.warn("Test {}: script had text \"{}\"", result.testName, entireScript);
                throw new WrapperException("Script does not match expected script.");
            }
            result.setStatus(Status.OK);
        }),
        PERF_CGPM((result, manifest, management) -> {
            TestEnvironment environment = new TestEnvironment();
            ArrayList<ScriptLine> lines = new ArrayList<>();
            final String line = "cgpm.testMethod";
            for (int i = 0; i < 10000; i++) {
                lines.add(new ScriptLine(line + i % 7 + "()"));
            }
            StreamExecutionScript script = new StreamExecutionScript(lines, environment);
            String cgpmType = manifest.getAsJsonPrimitive("cgpm").getAsString();
            switch (cgpmType) {
                case "easy" -> {
                    script.put("cgpm", new TestObjects.TestEasyCGPM());
                }
                case "jw" -> {
                    script.put("cgpm", new TestObjects.TestJWCGPM());
                }
                case "basic" -> {
                    script.put("cgpm", new TestObjects.TestBasicCGPM());
                }
                default -> {
                    result.failWith(new Throwable("Invalid cgpm type: " + cgpmType));
                    return;
                }
            }
            CountDownLatch threadLock = new CountDownLatch(1);
            result.profiler.finishState("Script created");
            CogwheelExecutor.schedule(() -> {
                script.setOnEnd(threadLock::countDown);
                script.setErrorHandler(scriptException -> {
                    result.failWith(new WrapperException("Script exception got caught", scriptException));
                    script.haltExecution();
                    threadLock.countDown();
                });
                try {
                    Thread.sleep(2); // Give some time for test-worker to start waiting
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                script.lineDispatcher();
            });
            boolean executed = threadLock.await(15000, TimeUnit.MILLISECONDS);
            if (executed) {
                result.profiler.finishState("Executed");
                result.setStatus(Status.OK);
            } else {
                result.profiler.finishState("Timed out (>15000ms)");
                result.setStatus(Status.TEST_FAILED);
            }
        });

        public final TestTypeHandler handler;
        TestType(TestTypeHandler handler) {
            this.handler = handler;
        }
    }
}
