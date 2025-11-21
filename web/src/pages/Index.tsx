import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { MessageSquare, Phone, Code2, Zap, Shield, Smartphone, Download, Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import heroPhone from "@/assets/hero-phone.png";
import logoBranca from "@/assets/logo-branca.png";
import logoPreta from "@/assets/logo-preta.png";

const Index = () => {
  const { theme, setTheme } = useTheme();

  const features = [
    {
      icon: MessageSquare,
      title: "Mensagens Instantâneas",
      description: "Chat em tempo real com suporte a markdown e snippets de código"
    },
    {
      icon: Phone,
      title: "Ligações HD",
      description: "Chamadas de voz e vídeo com qualidade cristalina"
    },
    {
      icon: Code2,
      title: "Feito para Devs",
      description: "Compartilhe código com syntax highlighting integrado"
    },
    {
      icon: Zap,
      title: "Ultra Rápido",
      description: "Performance otimizada para não atrapalhar seu workflow"
    },
    {
      icon: Shield,
      title: "Segurança Total",
      description: "Criptografia de ponta a ponta em todas as conversas"
    },
    {
      icon: Smartphone,
      title: "Multiplataforma",
      description: "Sincronize em todos os seus dispositivos"
    }
  ];

  const stores = [
    { name: "App Store", platform: "iOS" },
    { name: "Google Play", platform: "Android" },
    { name: "Web App", platform: "Browser" }
  ];

  return (
    <div className="min-h-screen bg-background overflow-x-hidden">
      {/* Theme Toggle */}
      <div className="fixed top-6 right-6 z-50">
        <Button
          variant="outline"
          size="icon"
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          className="rounded-full bg-card border-border hover:bg-accent"
        >
          <Sun className="h-5 w-5 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
          <Moon className="absolute h-5 w-5 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
          <span className="sr-only">Alternar tema</span>
        </Button>
      </div>

      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center px-6 py-20">
        <div className="absolute inset-0 bg-gradient-to-b from-primary/30 via-primary-glow/10 to-background pointer-events-none" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,hsl(272_74%_31%/0.3),transparent_50%)]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_70%_80%,hsl(337_82%_59%/0.2),transparent_50%)]" />
        
        <div className="container mx-auto max-w-7xl relative z-10">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div className="space-y-8 text-center lg:text-left">
              <div className="space-y-4">
                <div className="flex items-center justify-center lg:justify-start gap-6">
                  <img 
                    src={theme === "dark" ? logoBranca : logoPreta} 
                    alt="Nebula Logo" 
                    className="h-20 md:h-24 lg:h-28 w-auto"
                  />
                  <h1 className="text-6xl md:text-7xl lg:text-8xl font-bold tracking-tight">
                    <span className="text-gradient">Nebula</span>
                  </h1>
                </div>
                <p className="text-xl md:text-2xl text-muted-foreground max-w-2xl mx-auto lg:mx-0">
                  A nova era da comunicação para desenvolvedores
                </p>
              </div>
              
              <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
                <Button 
                  size="lg" 
                  className="bg-gradient-to-r from-primary to-primary-glow hover:opacity-90 transition-opacity glow-effect text-lg px-8 py-6 font-semibold"
                >
                  <Download className="mr-2 h-5 w-5" />
                  Baixar Agora
                </Button>
                <Button 
                  size="lg" 
                  variant="outline"
                  className="border-primary/50 hover:bg-primary/10 text-lg px-8 py-6 font-semibold"
                >
                  Ver Features
                </Button>
              </div>

              <div className="pt-8">
                <p className="text-sm text-muted-foreground mb-4 font-medium">
                  Disponível em todas as plataformas
                </p>
                <div className="flex flex-wrap gap-4 justify-center lg:justify-start">
                  {stores.map((store) => (
                    <div 
                      key={store.platform}
                      className="flex items-center gap-2 px-4 py-2 bg-card border border-border rounded-lg hover:border-primary/50 transition-colors cursor-pointer"
                    >
                      <Download className="h-4 w-4 text-primary-glow" />
                      <div className="text-left">
                        <p className="text-xs text-muted-foreground">{store.platform}</p>
                        <p className="text-sm font-semibold">{store.name}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="relative lg:flex hidden justify-center items-center">
              <div className="absolute inset-0 bg-gradient-to-r from-primary to-primary-glow opacity-20 blur-3xl animate-pulse" />
              <img 
                src={heroPhone} 
                alt="Nebula App Interface" 
                className="relative z-10 w-full max-w-xl h-auto animate-float drop-shadow-2xl"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-24 px-6 relative">
        <div className="container mx-auto max-w-7xl">
          <div className="text-center mb-16 space-y-4">
            <h2 className="text-4xl md:text-5xl font-bold">
              Feito sob medida para <span className="text-gradient">desenvolvedores</span>
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Todas as ferramentas que você precisa para se comunicar sem perder o foco
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((feature, index) => (
              <Card 
                key={index}
                className="p-6 bg-card border-border hover:border-primary/50 transition-all hover:shadow-lg hover:shadow-primary/20 group"
              >
                <div className="mb-4">
                  <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-primary to-primary-glow flex items-center justify-center group-hover:scale-110 transition-transform">
                    <feature.icon className="h-6 w-6 text-primary-foreground" />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-2">{feature.title}</h3>
                <p className="text-muted-foreground">{feature.description}</p>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Security Section */}
      <section className="py-24 px-6 relative">
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-primary/5 to-transparent pointer-events-none" />
        <div className="container mx-auto max-w-6xl relative z-10">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div className="space-y-6">
              <div className="inline-block">
                <div className="px-4 py-2 rounded-full bg-primary/10 border border-primary/20 text-sm font-semibold text-primary-glow">
                  Segurança Máxima
                </div>
              </div>
              <h2 className="text-4xl md:text-5xl font-bold">
                Suas conversas estão <span className="text-gradient">100% protegidas</span>
              </h2>
              <p className="text-lg text-muted-foreground">
                Utilizamos criptografia de ponta a ponta em todas as mensagens e chamadas. Ninguém, nem mesmo nós, consegue acessar o conteúdo das suas conversas.
              </p>
              <div className="space-y-4">
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center flex-shrink-0">
                    <Shield className="h-4 w-4 text-primary-glow" />
                  </div>
                  <div>
                    <h3 className="font-semibold mb-1">Criptografia E2E</h3>
                    <p className="text-sm text-muted-foreground">Protocolo militar de criptografia em todas as comunicações</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center flex-shrink-0">
                    <Shield className="h-4 w-4 text-primary-glow" />
                  </div>
                  <div>
                    <h3 className="font-semibold mb-1">Zero Logs</h3>
                    <p className="text-sm text-muted-foreground">Não armazenamos histórico de mensagens em nossos servidores</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center flex-shrink-0">
                    <Shield className="h-4 w-4 text-primary-glow" />
                  </div>
                  <div>
                    <h3 className="font-semibold mb-1">Autenticação 2FA</h3>
                    <p className="text-sm text-muted-foreground">Proteção adicional com autenticação de dois fatores</p>
                  </div>
                </div>
              </div>
            </div>
            <div className="relative">
              <div className="absolute inset-0 bg-gradient-to-r from-primary to-primary-glow opacity-20 blur-3xl" />
              <Card className="relative p-8 bg-card/50 backdrop-blur-sm border-primary/30">
                <div className="space-y-4">
                  <div className="flex items-center gap-3 p-4 rounded-lg bg-background/50">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary-glow flex items-center justify-center">
                      <Shield className="h-5 w-5 text-white" />
                    </div>
                    <div className="flex-1">
                      <div className="h-2 bg-muted rounded-full w-3/4 mb-2" />
                      <div className="h-2 bg-muted rounded-full w-1/2" />
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-4 rounded-lg bg-background/50">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary-glow flex items-center justify-center">
                      <Shield className="h-5 w-5 text-white" />
                    </div>
                    <div className="flex-1">
                      <div className="h-2 bg-muted rounded-full w-2/3 mb-2" />
                      <div className="h-2 bg-muted rounded-full w-1/3" />
                    </div>
                  </div>
                  <div className="text-center pt-4">
                    <p className="text-sm font-medium text-primary-glow">Criptografia Ativa</p>
                  </div>
                </div>
              </Card>
            </div>
          </div>
        </div>
      </section>

      {/* Code Interpreter Section */}
      <section className="py-24 px-6 relative">
        <div className="container mx-auto max-w-6xl">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div className="order-2 lg:order-1">
              <Card className="p-6 bg-card border-primary/30 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-primary to-primary-glow opacity-20 blur-2xl" />
                <div className="relative space-y-4">
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Code2 className="h-4 w-4" />
                    <span>main.py</span>
                  </div>
                  <pre className="text-sm bg-background/50 p-4 rounded-lg overflow-x-auto">
                    <code className="text-primary-glow">{`def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

print(fibonacci(10))`}</code>
                  </pre>
                  <div className="flex items-center gap-2 pt-2">
                    <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                    <span className="text-sm text-muted-foreground">Resultado: 55</span>
                  </div>
                </div>
              </Card>
            </div>
            <div className="space-y-6 order-1 lg:order-2">
              <div className="inline-block">
                <div className="px-4 py-2 rounded-full bg-primary/10 border border-primary/20 text-sm font-semibold text-primary-glow">
                  Interprete de Código
                </div>
              </div>
              <h2 className="text-4xl md:text-5xl font-bold">
                Execute código <span className="text-gradient">direto no chat</span>
              </h2>
              <p className="text-lg text-muted-foreground">
                Compartilhe e execute snippets de código diretamente nas conversas. Suporte para múltiplas linguagens com syntax highlighting e resultados em tempo real.
              </p>
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center">
                    <Code2 className="h-4 w-4 text-primary-glow" />
                  </div>
                  <div>
                    <p className="font-semibold">Múltiplas Linguagens</p>
                    <p className="text-sm text-muted-foreground">Python, JavaScript, TypeScript, Java, C++ e mais</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center">
                    <Zap className="h-4 w-4 text-primary-glow" />
                  </div>
                  <div>
                    <p className="font-semibold">Execução Instantânea</p>
                    <p className="text-sm text-muted-foreground">Veja os resultados em milissegundos</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center">
                    <MessageSquare className="h-4 w-4 text-primary-glow" />
                  </div>
                  <div>
                    <p className="font-semibold">Compartilhamento Fácil</p>
                    <p className="text-sm text-muted-foreground">Cole, execute e compartilhe com um clique</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 px-6 relative">
        <div className="absolute inset-0 bg-gradient-to-t from-primary/20 via-transparent to-transparent pointer-events-none" />
        <div className="container mx-auto max-w-4xl text-center relative z-10">
          <div className="bg-card border border-border rounded-2xl p-12 space-y-8">
            <h2 className="text-4xl md:text-5xl font-bold">
              Pronto para experimentar o <span className="text-gradient">futuro</span>?
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Junte-se a milhares de desenvolvedores que já estão usando o Nebula
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button 
                size="lg" 
                className="bg-gradient-to-r from-primary to-primary-glow hover:opacity-90 transition-opacity glow-effect text-lg px-8 py-6 font-semibold"
              >
                <Download className="mr-2 h-5 w-5" />
                Download Gratuito
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 px-6 border-t border-border">
        <div className="container mx-auto max-w-7xl">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <img 
              src={theme === "dark" ? logoBranca : logoPreta} 
              alt="Nebula Logo" 
              className="h-12 w-auto"
            />
            <p className="text-sm text-muted-foreground">
              © 2025 Nebula. Comunicação reimaginada para devs.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Index;
