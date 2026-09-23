package com.example.intranet_adm.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Enriquece os visitantes com o hostname registrado no DHCP do domínio.
 *
 * A consulta é somente leitura, executada em lote e protegida por cache para
 * não sobrecarregar o servidor DHCP nem bloquear a interface JavaFX.
 */
public final class DhcpHostnameResolver {
    private static final String DEFAULT_SERVER = "thor.idam.am.gov.br";
    private static final String DEFAULT_SCOPE = "10.46.0.0";
    private static final Duration POSITIVE_CACHE = Duration.ofMinutes(30);
    private static final Duration NEGATIVE_CACHE = Duration.ofMinutes(5);
    private static final long QUERY_TIMEOUT_SECONDS = 12;

    private final String server;
    private final String scope;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public DhcpHostnameResolver() {
        this.server = configuredServer();
        this.scope = configuredScope();
    }

    public boolean preencherNomes(List<String[]> visitantes) {
        if (visitantes == null || visitantes.isEmpty()
                || server == null || scope == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        Set<String> pendentes = new LinkedHashSet<>();
        boolean changed = false;

        for (String[] visitante : visitantes) {
            if (!precisaResolver(visitante)) continue;
            String ip = visitante[5].trim();
            CacheEntry entry = cache.get(ip);
            if (entry != null && entry.expiresAt() > now) {
                if (entry.hostname() != null) {
                    visitante[6] = entry.hostname();
                    changed = true;
                }
            } else {
                pendentes.add(ip);
            }
        }

        if (!pendentes.isEmpty()) {
            Map<String, String> encontrados = consultarDhcp(pendentes);
            long positiveExpiry = now + POSITIVE_CACHE.toMillis();
            long negativeExpiry = now + NEGATIVE_CACHE.toMillis();
            for (String ip : pendentes) {
                String hostname = encontrados.get(ip);
                cache.put(ip, new CacheEntry(
                        hostname,
                        hostname == null ? negativeExpiry : positiveExpiry));
            }
            for (String[] visitante : visitantes) {
                if (!precisaResolver(visitante)) continue;
                String hostname = encontrados.get(visitante[5].trim());
                if (hostname != null) {
                    visitante[6] = hostname;
                    changed = true;
                }
            }
        }

        return changed;
    }

    private Map<String, String> consultarDhcp(Set<String> ips) {
        Map<String, String> encontrados = new LinkedHashMap<>();
        StringBuilder array = new StringBuilder();
        for (String ip : ips) {
            if (array.length() > 0) array.append(',');
            array.append('\'').append(ip).append('\'');
        }

        String script = "$ErrorActionPreference='SilentlyContinue';"
                + "$server='" + server + "';"
                + "$wanted=@{};"
                + "@(" + array + ")|ForEach-Object{$wanted[$_]=1};"
                + "Get-DhcpServerv4Lease -ComputerName $server "
                + "-ScopeId '" + scope + "' -AllLeases "
                + "-ErrorAction SilentlyContinue|ForEach-Object{"
                + "$ip=$_.IPAddress.ToString();"
                + "if($wanted.ContainsKey($ip)-and $_.HostName){"
                + "[Console]::Out.WriteLine($ip+[char]124+$_.HostName)}}";

        Process process = null;
        try {
            process = new ProcessBuilder(
                    "powershell.exe",
                    "-NoLogo",
                    "-NoProfile",
                    "-NonInteractive",
                    "-Command",
                    script)
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return encontrados;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int separator = line.indexOf('|');
                    if (separator <= 0) continue;
                    String ip = line.substring(0, separator).trim();
                    String hostname = hostnameCurto(
                            line.substring(separator + 1).trim());
                    if (isIpv4(ip) && hostname != null) {
                        encontrados.put(ip, hostname);
                    }
                }
            }
        } catch (Exception error) {
            System.err.println("Consulta DHCP indisponível: " + error.getMessage());
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
        return encontrados;
    }

    private static boolean precisaResolver(String[] visitante) {
        if (visitante == null || visitante.length <= 6
                || visitante[5] == null
                || !isIpv4(visitante[5].trim())) {
            return false;
        }
        String maquina = visitante[6];
        if (maquina == null || maquina.isBlank()) return true;
        String normalized = maquina.trim().toLowerCase(Locale.ROOT);
        return "null".equals(normalized) || normalized.contains("informado");
    }

    private static String hostnameCurto(String hostname) {
        if (hostname == null || hostname.isBlank()) return null;
        String value = hostname.trim();
        if (value.endsWith(".")) value = value.substring(0, value.length() - 1);
        int domainStart = value.indexOf('.');
        return domainStart > 0 ? value.substring(0, domainStart) : value;
    }

    private static boolean isIpv4(String value) {
        if (value == null) return false;
        String[] parts = value.split("\\.", -1);
        if (parts.length != 4) return false;
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) return false;
            try {
                int octet = Integer.parseInt(part);
                if (octet < 0 || octet > 255) return false;
            } catch (NumberFormatException error) {
                return false;
            }
        }
        return true;
    }

    private static String configuredServer() {
        String value = System.getProperty("intranet.dhcp.server");
        if (value == null || value.isBlank()) {
            value = System.getenv("INTRANET_DHCP_SERVER");
        }
        if (value == null || value.isBlank()) value = DEFAULT_SERVER;
        value = value.trim();
        return value.matches("[A-Za-z0-9.-]+") ? value : null;
    }

    private static String configuredScope() {
        String value = System.getProperty("intranet.dhcp.scope");
        if (value == null || value.isBlank()) {
            value = System.getenv("INTRANET_DHCP_SCOPE");
        }
        if (value == null || value.isBlank()) value = DEFAULT_SCOPE;
        value = value.trim();
        return isIpv4(value) ? value : null;
    }

    private record CacheEntry(String hostname, long expiresAt) {
    }
}
