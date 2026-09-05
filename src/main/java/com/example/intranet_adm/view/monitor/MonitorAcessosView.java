package com.example.intranet_adm.view.monitor;

import com.example.intranet_adm.model.EstatisticasAcesso;
import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public final class MonitorAcessosView {
    private volatile boolean stopped;
    private final java.util.concurrent.atomic.AtomicBoolean loading = new java.util.concurrent.atomic.AtomicBoolean();
    private final IntranetAvisosClient client; private final BorderPane root=new BorderPane(); private XYChart.Series<String,Number> liveSeries; private final Label clock=new Label(); private Timeline clockTimer; private GridPane visitorGrid;
    private final Label online=v("—"), hoje=v("—"), total=v("—"), ultima=v("—"), status=new Label("● Aguardando conexão com o IDAM"); private Timeline timer; private Consumer<String> onMessage;
    public MonitorAcessosView(IntranetAvisosClient c){client=c; construir(); atualizar();}
    private Label v(String s){Label l=new Label(s);l.getStyleClass().add("monitor-value");return l;}
    private Label l(String s,String c){Label x=new Label(s);x.getStyleClass().add(c);return x;}
    private void construir(){VBox p=new VBox(18);p.setPadding(new Insets(4,0,20,0));HBox h=new HBox(12,l("Visão geral de acessos","card-title"),new Region());HBox.setHgrow(h.getChildren().get(1),Priority.ALWAYS);h.getChildren().add(l("Atualização a cada 30 s","monitor-live"));p.getChildren().addAll(h,hero(),stats(),lower());root.setCenter(p);timer=new Timeline(new KeyFrame(Duration.seconds(30),e->atualizar()));timer.setCycleCount(Animation.INDEFINITE);timer.play();clockTimer=new Timeline(new KeyFrame(Duration.seconds(1),e->clock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss")))));clockTimer.setCycleCount(Animation.INDEFINITE);clockTimer.play();}
    private Node hero(){HBox b=new HBox(24);b.getStyleClass().add("monitor-card");b.setPadding(new Insets(20));VBox m=new VBox(8);m.setAlignment(Pos.CENTER);HBox.setHgrow(m,Priority.ALWAYS);m.getChildren().addAll(l("♙","monitor-icon"),online,l("Pessoas navegando na Intranet agora","monitor-caption"),status);AreaChart<String,Number> a=new AreaChart<>(new CategoryAxis(),new NumberAxis());a.setLegendVisible(false);a.setAnimated(false);a.setCreateSymbols(false);a.setPrefHeight(210);liveSeries=new XYChart.Series<>();a.getData().add(liveSeries);b.getChildren().addAll(m,a);return b;}
    private Node stats(){HBox r=new HBox(16);r.getChildren().addAll(card("◷",hoje,"Acessos hoje","Acessos registrados no dia"),card("♙",total,"Total de visitantes","Total informado pela Intranet"),card("↗",ultima,"Última conexão","● Conexão mais recente registrada"));return r;}
    private VBox card(String i,Label n,String t,String d){VBox c=new VBox(7);c.getStyleClass().add("monitor-card");c.setPadding(new Insets(16));HBox.setHgrow(c,Priority.ALWAYS);c.getChildren().addAll(l(i,"monitor-icon-small"),n,l(t,"card-title"),l(d,"monitor-detail"));return c;}
    private Node lower(){VBox t=new VBox(10);t.getStyleClass().add("monitor-card");t.setPadding(new Insets(16));HBox title=new HBox(l("Visitantes ativos","card-title"));Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);clock.getStyleClass().add("monitor-clock");title.getChildren().addAll(spacer,clock);t.getChildren().addAll(title,l("Usuários navegando na Intranet neste momento.","page-description"));visitorGrid=new GridPane();visitorGrid.setHgap(28);visitorGrid.setVgap(12);t.getChildren().add(visitorGrid);preencherVisitantes(java.util.List.of());return t;}
    private void preencherVisitantes(java.util.List<String[]> pessoas){visitorGrid.getChildren().clear();String[] h={"Usuário","Departamento","Página atual","Tempo ativo","Navegador"};for(int x=0;x<h.length;x++)visitorGrid.add(l(h[x],"table-head"),x,0);for(int y=0;y<pessoas.size();y++){final String nome=pessoas.get(y)[0];for(int x=0;x<5;x++){Label c=l((x==0?"●  ":"")+(x < pessoas.get(y).length ? pessoas.get(y)[x] : "—"),"table-cell");c.setOnMouseClicked(e->status.setText("● Visitante selecionado: "+nome));visitorGrid.add(c,x,y+1);}}}
    public void atualizar(){if(stopped || !loading.compareAndSet(false,true)) return; Thread worker = new Thread(()->{try{EstatisticasAcesso d=client.buscarEstatisticasAcesso();java.util.List<String[]> pessoas=client.buscarVisitantes();javafx.application.Platform.runLater(()->{if(stopped) return; online.setText(""+d.getOnlineAgora());hoje.setText(""+d.getAcessosHoje());total.setText(""+d.getTotalVisitantes());ultima.setText(d.getUltimaConexao()==null?"—":d.getUltimaConexao());preencherVisitantes(pessoas);status.setText("● Atualizado às "+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));if(liveSeries!=null){liveSeries.getData().add(new XYChart.Data<>(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),d.getOnlineAgora()));if(liveSeries.getData().size()>20)liveSeries.getData().remove(0);}});}catch(Exception e){javafx.application.Platform.runLater(()->{if(!stopped) status.setText("● Sem conexão com o IDAM");});}finally{loading.set(false);}}); worker.setDaemon(true); worker.start();}
    public void pararAtualizacaoAutomatica(){stopped=true;if(timer!=null)timer.stop();if(clockTimer!=null)clockTimer.stop();}
    public void setOnMessage(Consumer<String> c){onMessage=c;}
    public Node getView(){return root;}
    public BorderPane getRoot(){return root;}
}
