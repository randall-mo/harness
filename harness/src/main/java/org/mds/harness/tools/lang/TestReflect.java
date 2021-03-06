package org.mds.harness.tools.lang;

import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.harness.common.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class TestReflect {
    private final static Logger log = LoggerFactory.getLogger(TestReflect.class);
    private static String S = "";

    public static String newTag(String line) {
        return "fdjkfldlf" + line;
    }

    public void runReflect1(PerfConfig configuration) throws Exception {
        log.info("Start test .....");
        final Method method = this.getClass().getMethod("newTag", new Class[]{String.class});
        PerfTester perfTester = new PerfTester("Test reflect 1", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                try {
                    method.invoke(TestReflect.class, "");
                } catch (Exception ex) {
                    log.error("Failed to execute method:" + ex);
                }
                return 1;
            }
        });
        perfTester.run();
    }

    public void runReflect2(PerfConfig configuration) throws Exception {
        log.info("Start test .....");
        PerfTester perfTester = new PerfTester("Test reflect 2", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                try {
                    Method method = TestReflect.class.getMethod("newTag", new Class[]{String.class});
                    method.invoke(TestReflect.class, "");
                } catch (Exception ex) {
                    log.error("Failed to execute method:" + ex);
                }
                return 1;
            }
        });
        perfTester.run();
    }

    public void runNormal(PerfConfig configuration) throws Exception {
        log.info("Start test .....");
        PerfTester perfTester = new PerfTester("Test normal", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                try {
                    TestReflect.newTag("");
                } catch (Exception ex) {
                    log.error("Failed to execute method:" + ex);
                }
                return 1;
            }
        });
        perfTester.run();
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestReflect.class,
                PerfConfig.class, "testReflect.conf");
    }
}
