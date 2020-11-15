/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 *
 * @author bmayr
 */
// Recursive Action for forkJoinFramework from Java7
public class LinkFinderAction extends RecursiveAction {

    private String url;
    private ILinkHandler cr;
    /**
     * Used for statistics
     */
    private static final long t0 = System.nanoTime();

    public LinkFinderAction(String url, ILinkHandler cr) {
        this.url = url;
        this.cr = cr;
    }

    @Override
    public void compute() {
        // if size of crawler exceeds 100 
        if (cr.size() <= 100) {
            // if crawler has not visited url yet
            if (!cr.visited(url)) {
                // Create new list of recursiveActions
                List<LinkFinderAction> recAct = new ArrayList<>();
                // extract all links from url
                Parser parser;
                NodeFilter filter = new NodeClassFilter(LinkTag.class);
                NodeList list;
                if (0 <= cr.size()) {
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
                    for (int i = 0; i < list.size(); i++) {
                        Node currentLinkNode = list.elementAt(i);
                        if (currentLinkNode instanceof LinkTag) {
                            LinkTag lt = (LinkTag) currentLinkNode;
                            recAct.add(new LinkFinderAction(lt.getLink(), cr));
                        }
                    }
                    cr.addVisited(url);
                    // Do not forget to call ìnvokeAll on the actions!     
                    invokeAll(recAct);
                } catch (ParserException ex) {
                    Logger.getLogger(LinkFinderAction.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } // print elapsed time for statistics
        else {
            System.out.println("elapsed time for statistics: " + (System.nanoTime() - t0));
            System.exit(0);
        }
    }
}
