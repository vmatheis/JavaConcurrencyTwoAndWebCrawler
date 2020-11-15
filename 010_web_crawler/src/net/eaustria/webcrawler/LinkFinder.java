/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

/**
 *
 * @author bmayr
 */
import java.net.URL;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import java.util.ArrayList;
import java.util.List;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.util.ParserException;

public class LinkFinder implements Runnable {

    private String url;
    private ILinkHandler linkHandler;
    /**
     * Used fot statistics
     */
    private static final long t0 = System.nanoTime();

    public LinkFinder(String url, ILinkHandler handler) {
        this.url = url;
        this.linkHandler = handler;
    }

    @Override
    public void run() {
        getSimpleLinks(url);
    }

    private void getSimpleLinks(String url) {
        // If size of link handler equals 500 -> else
        if (linkHandler.size() < 100) {
            // 1. if url not already visited, visit url with linkHandler
            if (!linkHandler.visited(url)) {
                // get url and Parse Website
                Parser parser;
                NodeFilter filter;
                NodeList list;
                filter = new NodeClassFilter(LinkTag.class);
                if (0 <= linkHandler.size()) {
                    filter = new AndFilter(
                            filter,
                            new NodeFilter() {
                        public boolean accept(Node node) {
                            return (((LinkTag) node).isHTTPSLink());
                        }
                    });
                }
                try {
                    parser = new Parser(url);
                    list = parser.extractAllNodesThatMatch(filter);
                    //extract all URLs and add url to list of urls which should be visited only if link is not empty and url has not been visited before
                    for (int i = 0; i < list.size(); i++) {
                        Node currentLinkNode = list.elementAt(i);
                        if (currentLinkNode instanceof LinkTag) {
                            LinkTag lt = (LinkTag) currentLinkNode;
                            if ((!linkHandler.visited(lt.getLink())) && (!lt.getLink().equals(""))) {
                                String link = lt.getLink();
                                linkHandler.queueLink(link);
                            }
                        }
                    }
                } catch (ParserException e) {
                    System.out.println("ParserException");
                } catch (Exception ex) {
                    System.out.println("Exception");
                }
                linkHandler.addVisited(url);
            }
        } // print time elapsed for statistics
        else {
            System.out.println("elapsed time for statistics: " + (System.nanoTime() - t0));
            System.exit(0);
        }
    }
}
