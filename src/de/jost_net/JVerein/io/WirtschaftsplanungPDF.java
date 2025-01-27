package de.jost_net.JVerein.io;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungNode;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WirtschaftsplanungPDF {
    private Map<WirtschaftsplanungNode, Double> sollSummen;


    public WirtschaftsplanungPDF(List<WirtschaftsplanungNode> einnahmenList,
                                 List<WirtschaftsplanungNode> ausgabenList, File file,
                                 Wirtschaftsplan wirtschaftsplan) throws ApplicationException {
        sollSummen = new HashMap<>();
        try {
            double sollEinnahmenGesamt = calculateSolls(einnahmenList);
            double sollAusgabenGesamt = calculateSolls(ausgabenList);

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            String subtitle = new JVDateFormatTTMMJJJJ().format(wirtschaftsplan.getDatumVon()) + " - " + new JVDateFormatTTMMJJJJ().format(wirtschaftsplan.getDatumBis());
            int size = einnahmenList.stream().mapToInt(WirtschaftsplanungNode::anzahlLeafs).sum() + ausgabenList.stream().mapToInt(WirtschaftsplanungNode::anzahlLeafs).sum();
            Reporter reporter = new Reporter(fileOutputStream, "Wirtschaftsplan", subtitle, size);

            Paragraph detailParagraph = new Paragraph("\n Detailansicht", Reporter.getFreeSans(11));
            reporter.add(detailParagraph);

            reporter.addHeaderColumn("Buchungsart", Element.ALIGN_CENTER, 90,
                    BaseColor.LIGHT_GRAY);
            reporter.addHeaderColumn("Posten", Element.ALIGN_CENTER, 90, BaseColor.LIGHT_GRAY);
            reporter.addHeaderColumn("Einnahmen Soll", Element.ALIGN_CENTER, 45,
                    BaseColor.LIGHT_GRAY);
            reporter.addHeaderColumn("Ausgaben Soll", Element.ALIGN_CENTER, 45,
                    BaseColor.LIGHT_GRAY);
            reporter.createHeader();

            reporter.addColumn("Einnahmen", Element.ALIGN_CENTER, new BaseColor(220, 220, 220), 4);

            einnahmenList.forEach(einnahme -> {

                try {
                    reporter.addColumn(einnahme.getBuchungsklasse().getBezeichnung(),
                            Element.ALIGN_LEFT, new BaseColor(220, 220, 220), 2);
                    reporter.addColumn(sollSummen.get(einnahme));
                    reporter.addColumn("", Element.ALIGN_CENTER);

                    iterateOverNodes(einnahme.getChildren(), reporter, true);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });

            reporter.addColumn("Ausgaben", Element.ALIGN_CENTER, new BaseColor(220, 220, 220), 3);

            ausgabenList.forEach(ausgabe -> {
                try {
                    reporter.addColumn(ausgabe.getBuchungsklasse().getBezeichnung(),
                            Element.ALIGN_LEFT, new BaseColor(220, 220, 220), 2);
                    reporter.addColumn("", Element.ALIGN_CENTER);
                    reporter.addColumn(sollSummen.get(ausgabe));

                    iterateOverNodes(ausgabe.getChildren(), reporter, false);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });

            reporter.closeTable();

            Paragraph zusammenfassungParagraph = new Paragraph("\n Zusammenfassung", Reporter.getFreeSans(11));
            reporter.add(zusammenfassungParagraph);

            reporter.addHeaderColumn("Einnahmen Soll", Element.ALIGN_CENTER, 40, BaseColor.LIGHT_GRAY);
            reporter.addHeaderColumn("Ausgaben Soll", Element.ALIGN_CENTER, 40, BaseColor.LIGHT_GRAY);
            reporter.addHeaderColumn("Saldo", Element.ALIGN_CENTER, 40, BaseColor.LIGHT_GRAY);

            reporter.addColumn(sollEinnahmenGesamt);
            reporter.addColumn(sollAusgabenGesamt);
            reporter.addColumn(sollEinnahmenGesamt - sollAusgabenGesamt);

            reporter.closeTable();
            reporter.close();
            fileOutputStream.close();
            FileViewer.show(file);
        }
        catch (Exception e)
        {
            Logger.error("error while creating report", e);
            throw new ApplicationException(e);
        }
    }

    private double calculateSolls(List<WirtschaftsplanungNode> nodeList) {
        return nodeList.stream().mapToDouble(node -> {
            try {
                double soll = calculateSolls(node.getChildren());
                sollSummen.put(node, soll);
                return soll;
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }).sum();
    }

    private double calculateSolls(GenericIterator iterator) throws RemoteException {
        double soll = 0;

        while (iterator.hasNext()) {
            WirtschaftsplanungNode currentNode = (WirtschaftsplanungNode) iterator.next();
            double currentSoll;

            if (currentNode.getType().equals(WirtschaftsplanungNode.Type.POSTEN))
            {
                currentSoll = currentNode.getSoll();
            }
            else {
                currentSoll = calculateSolls(currentNode.getChildren());
            }

            sollSummen.put(currentNode, currentSoll);
            soll += currentSoll;
        }

        return soll;
    }

    private void iterateOverNodes(GenericIterator iterator, Reporter reporter, boolean einnahme) throws RemoteException {
        while (iterator.hasNext()) {
            WirtschaftsplanungNode currentNode = (WirtschaftsplanungNode) iterator.next();

            switch (currentNode.getType()) {
                case BUCHUNGSART -> {
                    reporter.addColumn(currentNode.getBuchungsart().getBezeichnung(), Element.ALIGN_LEFT);
                    reporter.addColumn("", Element.ALIGN_CENTER);
                }
                case POSTEN -> {
                    reporter.addColumn("", Element.ALIGN_CENTER);
                    reporter.addColumn(currentNode.getWirtschaftsplanItem().getPosten(), Element.ALIGN_LEFT);
                }
            }
            if (einnahme) {
                reporter.addColumn(sollSummen.get(currentNode));
                reporter.addColumn("", Element.ALIGN_CENTER);
            }
            else {
                reporter.addColumn("", Element.ALIGN_CENTER);
                reporter.addColumn(sollSummen.get(currentNode));
            }
        }
    }
}
