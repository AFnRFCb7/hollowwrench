package hollowwrench;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.err;
import
    static
    net.sourceforge.cobertura.coveragedata
    .ProjectData
    .saveGlobalProjectData;

/**
 * Saves coverage data on shutdown.
 **/
class CoveredMain extends Main {
    static {
        new CoveredMain() {
            {
                err.println("COVERED");
            }
        };
        getRuntime().addShutdownHook(new Thread() {
                /**
                 * Save coverage data.
                 **/
                @Override
                public void run() {
                    saveGlobalProjectData();
                }
            });
    }
}
