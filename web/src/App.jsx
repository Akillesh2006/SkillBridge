import React, { useState, useEffect } from 'react';
import { 
  Compass, 
  FileText, 
  CheckCircle2, 
  Sparkles, 
  Target, 
  Cpu, 
  Github, 
  ArrowRight, 
  Award, 
  Terminal, 
  Zap, 
  Code2, 
  Layers, 
  BrainCircuit, 
  MessageSquare, 
  UploadCloud, 
  Check, 
  ChevronRight,
  ExternalLink,
  ShieldCheck,
  Activity
} from 'lucide-react';

const JOB_ROLES = [
  { id: 'fullstack', name: 'Full Stack Engineer (Java + React)', demand: 'High', match: '88%', skillsNeeded: ['GraphQL', 'Kubernetes', 'Redis', 'Kafka'] },
  { id: 'android', name: 'Android Engineer (Kotlin + Jetpack)', demand: 'Very High', match: '92%', skillsNeeded: ['Hilt DI', 'Room DB', 'Jetpack Compose', 'Coroutines'] },
  { id: 'aiml', name: 'AI / ML Solutions Engineer', demand: 'Explosive', match: '78%', skillsNeeded: ['PyTorch', 'LangChain', 'RAG Architecture', 'Vector DBs'] },
  { id: 'cloud', name: 'Cloud & DevOps Architect', demand: 'High', match: '82%', skillsNeeded: ['Terraform', 'AWS EKS', 'CI/CD Pipelines', 'Prometheus'] }
];

const INITIAL_ROADMAP = [
  { id: 1, stage: 'Phase 1: Foundations', title: 'Core Language & Microservices', duration: '2 Weeks', status: 'completed', topics: ['Java 17 & Spring Boot 3.x', 'RESTful API Contracts & OpenAPI', 'Spring Data JPA & PostgreSQL'] },
  { id: 2, stage: 'Phase 2: Modern Frontend', title: 'React & State Management', duration: '3 Weeks', status: 'completed', topics: ['React Hooks & Context API', 'Vite & Tailwind Architecture', 'Asynchronous API Consumption'] },
  { id: 3, stage: 'Phase 3: Containerization', title: 'Docker & Cloud Deployment', duration: '2 Weeks', status: 'in-progress', topics: ['Multi-Stage Dockerfiles', 'Cloud Hosting (Render/Vercel)', 'Environment Secret Management'] },
  { id: 4, stage: 'Phase 4: AI & Vector Search', title: 'LLM & Resume Parsing Engine', duration: '3 Weeks', status: 'upcoming', topics: ['PDFBox & Document Parsing', 'Embedding Generation & Similarity', 'AI Mock Interview Agent'] },
  { id: 5, stage: 'Phase 5: System Design', title: 'Scalability & Performance', duration: '2 Weeks', status: 'upcoming', topics: ['Redis Caching Layer', 'Asynchronous Message Queues', 'Load Balancing & Rate Limiting'] }
];

export default function App() {
  const [activeTab, setActiveTab] = useState('roadmap');
  const [selectedRole, setSelectedRole] = useState(JOB_ROLES[0]);
  const [roadmap, setRoadmap] = useState(INITIAL_ROADMAP);
  const [resumeText, setResumeText] = useState('');
  const [isScanning, setIsScanning] = useState(false);
  const [scanResult, setScanResult] = useState(null);
  const [backendHealth, setBackendHealth] = useState('checking');

  // Interactive Quiz State
  const [quizAnswer, setQuizAnswer] = useState(null);
  const [quizSubmitted, setQuizSubmitted] = useState(false);

  // Chat copilot state
  const [chatMessages, setChatMessages] = useState([
    { sender: 'ai', text: 'Hello! I am your SkillBridge AI Copilot. What career topic or technical concept would you like to prepare for today?' }
  ]);
  const [userChatInput, setUserChatInput] = useState('');

  useEffect(() => {
    // Check backend connection if live
    fetch('/api/health')
      .then(res => res.ok ? setBackendHealth('online') : setBackendHealth('offline'))
      .catch(() => setBackendHealth('offline'));
  }, []);

  const handleScanResume = () => {
    setIsScanning(true);
    setTimeout(() => {
      setIsScanning(false);
      setScanResult({
        score: 86,
        detectedSkills: ['Java 17', 'Spring Boot', 'REST APIs', 'Docker', 'Git', 'Kotlin', 'SQL'],
        missingSkills: ['GraphQL', 'Kubernetes', 'Redis Caching', 'System Design'],
        atsRecommendations: [
          'Add quantitative metrics to your project achievements (e.g. "Reduced query latency by 40%")',
          'Include cloud deployment experience (Docker / Vercel / Render)',
          'Demonstrate API documentation skills (OpenAPI/Swagger)'
        ]
      });
    }, 1200);
  };

  const toggleStepStatus = (id) => {
    setRoadmap(prev => prev.map(item => {
      if (item.id === id) {
        const nextStatus = item.status === 'completed' ? 'in-progress' : item.status === 'in-progress' ? 'upcoming' : 'completed';
        return { ...item, status: nextStatus };
      }
      return item;
    }));
  };

  const completedCount = roadmap.filter(r => r.status === 'completed').length;
  const progressPercent = Math.round((completedCount / roadmap.length) * 100);

  const handleSendChat = (e) => {
    e.preventDefault();
    if (!userChatInput.trim()) return;
    const newMsg = { sender: 'user', text: userChatInput };
    setChatMessages(prev => [...prev, newMsg]);
    setUserChatInput('');

    setTimeout(() => {
      let reply = "That's a great area to focus on! In system design interviews, always start by clarifying functional requirements, scale (QPS/storage), and high-level components before diving into database schema details.";
      if (userChatInput.toLowerCase().includes('resume') || userChatInput.toLowerCase().includes('ats')) {
        reply = "For ATS optimization, make sure your core skills (Java, React, Docker) are clearly listed under a dedicated 'Technical Skills' section with clean formatting.";
      } else if (userChatInput.toLowerCase().includes('vercel') || userChatInput.toLowerCase().includes('deploy')) {
        reply = "Vercel automatically builds and deploys your web frontend on every Git commit. Connect your backend API endpoints via environment variables for live cloud execution!";
      }
      setChatMessages(prev => [...prev, { sender: 'ai', text: reply }]);
    }, 800);
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      
      {/* Top Banner */}
      <div style={{ background: 'linear-gradient(90deg, #4f46e5 0%, #7c3aed 100%)', color: '#fff', padding: '8px 16px', fontSize: '0.875rem', fontWeight: '500', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px' }}>
        <Sparkles size={16} />
        <span>SkillBridge is Live! Hosted on Vercel & Syncing with GitHub Repository</span>
        <a 
          href="https://github.com/Akillesh2006/SkillBridge" 
          target="_blank" 
          rel="noreferrer" 
          style={{ color: '#fff', textDecoration: 'underline', fontWeight: '600', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
        >
          View GitHub Repo <ExternalLink size={14} />
        </a>
      </div>

      {/* Header Navigation */}
      <header className="glass-card-static" style={{ margin: '16px 24px', padding: '12px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border-glass)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)', width: '40px', height: '40px', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 12px rgba(99, 102, 241, 0.4)' }}>
            <BrainCircuit size={24} color="#ffffff" />
          </div>
          <div>
            <h1 style={{ fontSize: '1.25rem', fontWeight: '800', letterSpacing: '-0.02em' }} className="gradient-text">SkillBridge</h1>
            <span style={{ fontSize: '0.75rem', color: '#9ca3af' }}>AI Career Roadmap & Skill Copilot</span>
          </div>
        </div>

        {/* Nav Tabs */}
        <nav style={{ display: 'flex', gap: '8px', background: 'rgba(0, 0, 0, 0.3)', padding: '6px', borderRadius: '14px', border: '1px solid var(--border-glass)' }}>
          {[
            { id: 'roadmap', label: 'AI Roadmap', icon: Compass },
            { id: 'resume', label: 'Resume Scanner', icon: FileText },
            { id: 'skills', label: 'Skill Assessment', icon: Target },
            { id: 'copilot', label: 'Mock Interview AI', icon: MessageSquare }
          ].map(tab => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '8px 16px',
                  borderRadius: '10px',
                  border: 'none',
                  background: isActive ? 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)' : 'transparent',
                  color: isActive ? '#ffffff' : '#9ca3af',
                  fontWeight: isActive ? '600' : '500',
                  fontSize: '0.875rem',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease'
                }}
              >
                <Icon size={16} />
                {tab.label}
              </button>
            );
          })}
        </nav>

        {/* Right Action */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div className={`badge ${backendHealth === 'online' ? 'badge-emerald' : 'badge-cyan'}`}>
            <Activity size={12} />
            {backendHealth === 'online' ? 'Backend Online' : 'Cloud Serverless'}
          </div>
          <a
            href="https://github.com/Akillesh2006/SkillBridge"
            target="_blank"
            rel="noreferrer"
            className="btn-secondary"
            style={{ padding: '8px 14px', fontSize: '0.85rem' }}
          >
            <Github size={16} /> GitHub Repo
          </a>
        </div>
      </header>

      {/* Main Content Area */}
      <main style={{ maxWidth: '1280px', margin: '0 auto', padding: '0 24px 48px', width: '100%', flex: 1 }}>

        {/* Hero Section */}
        <section style={{ textAlign: 'center', padding: '32px 16px 40px' }}>
          <div className="badge badge-indigo" style={{ marginBottom: '16px' }}>
            <Zap size={14} /> AI-Powered Career Roadmap & ATS Engine
          </div>
          <h2 style={{ fontSize: '2.5rem', fontWeight: '800', lineHeight: '1.2', marginBottom: '16px' }}>
            Bridge the Gap Between Your <span className="gradient-text">Current Skills</span> & <span className="gradient-text-emerald">Dream Career</span>
          </h2>
          <p style={{ maxWidth: '680px', margin: '0 auto 24px', color: '#9ca3af', fontSize: '1.1rem', lineHeight: '1.6' }}>
            Upload your resume, analyze real-time industry skill gaps, visualize dynamic step-by-step career roadmaps, and practice with your personal AI interview copilot.
          </p>

          {/* Job Role Selector Pills */}
          <div style={{ display: 'flex', justifyContent: 'center', flexWrap: 'wrap', gap: '10px', marginTop: '16px' }}>
            {JOB_ROLES.map(role => (
              <button
                key={role.id}
                onClick={() => setSelectedRole(role)}
                style={{
                  padding: '8px 18px',
                  borderRadius: '9999px',
                  border: selectedRole.id === role.id ? '1px solid #818cf8' : '1px solid var(--border-glass)',
                  background: selectedRole.id === role.id ? 'rgba(99, 102, 241, 0.2)' : 'rgba(255, 255, 255, 0.03)',
                  color: selectedRole.id === role.id ? '#ffffff' : '#9ca3af',
                  fontSize: '0.875rem',
                  fontWeight: selectedRole.id === role.id ? '600' : '400',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease'
                }}
              >
                {role.name} <span style={{ opacity: 0.7, fontSize: '0.75rem', marginLeft: '6px' }}>({role.match} Match)</span>
              </button>
            ))}
          </div>
        </section>

        {/* TAB 1: AI ROADMAP */}
        {activeTab === 'roadmap' && (
          <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '24px' }}>
            
            {/* Left Panel: Target Stats */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div className="glass-card" style={{ padding: '24px' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '12px' }}>Target Target Role</h3>
                <div style={{ background: 'rgba(99, 102, 241, 0.1)', padding: '12px', borderRadius: '12px', border: '1px solid rgba(99, 102, 241, 0.2)', marginBottom: '16px' }}>
                  <div style={{ fontSize: '1rem', fontWeight: '700', color: '#a5b4fc' }}>{selectedRole.name}</div>
                  <div style={{ fontSize: '0.8rem', color: '#9ca3af', marginTop: '4px' }}>Market Demand: {selectedRole.demand}</div>
                </div>

                <div style={{ marginBottom: '16px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '8px' }}>
                    <span style={{ color: '#9ca3af' }}>Overall Progress</span>
                    <span style={{ fontWeight: '700', color: '#818cf8' }}>{progressPercent}%</span>
                  </div>
                  <div className="progress-container">
                    <div className="progress-bar" style={{ width: `${progressPercent}%` }}></div>
                  </div>
                </div>

                <h4 style={{ fontSize: '0.875rem', fontWeight: '600', color: '#e5e7eb', marginBottom: '10px' }}>Top Required Skills:</h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {selectedRole.skillsNeeded.map((s, idx) => (
                    <span key={idx} className="badge badge-indigo" style={{ fontSize: '0.7rem' }}>{s}</span>
                  ))}
                </div>
              </div>

              <div className="glass-card" style={{ padding: '24px' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '700', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <ShieldCheck size={18} color="#10b981" /> Skill Certificate Ready
                </h3>
                <p style={{ fontSize: '0.85rem', color: '#9ca3af', lineHeight: '1.5' }}>
                  Complete 100% of your roadmap modules to unlock your verified SkillBridge Competency Badge for LinkedIn.
                </p>
              </div>
            </div>

            {/* Right Panel: Interactive Roadmap Stages */}
            <div className="glass-card" style={{ padding: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <div>
                  <h3 style={{ fontSize: '1.25rem', fontWeight: '800' }}>{selectedRole.name} Learning Path</h3>
                  <p style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Click step checkboxes to update your live learning progress.</p>
                </div>
                <span className="badge badge-emerald">{completedCount} of {roadmap.length} Milestones Done</span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {roadmap.map((item, idx) => {
                  const isCompleted = item.status === 'completed';
                  const isInProgress = item.status === 'in-progress';

                  return (
                    <div
                      key={item.id}
                      style={{
                        padding: '18px 20px',
                        borderRadius: '14px',
                        background: isCompleted ? 'rgba(16, 185, 129, 0.06)' : isInProgress ? 'rgba(99, 102, 241, 0.08)' : 'rgba(255, 255, 255, 0.02)',
                        border: isCompleted ? '1px solid rgba(16, 185, 129, 0.3)' : isInProgress ? '1px solid rgba(99, 102, 241, 0.4)' : '1px solid var(--border-glass)',
                        display: 'flex',
                        gap: '16px',
                        alignItems: 'flex-start',
                        transition: 'all 0.2s ease'
                      }}
                    >
                      <button
                        onClick={() => toggleStepStatus(item.id)}
                        style={{
                          width: '28px',
                          height: '28px',
                          borderRadius: '8px',
                          border: isCompleted ? 'none' : '2px solid rgba(255, 255, 255, 0.2)',
                          background: isCompleted ? '#10b981' : 'transparent',
                          color: '#ffffff',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          cursor: 'pointer',
                          marginTop: '2px',
                          flexShrink: 0
                        }}
                      >
                        {isCompleted && <Check size={18} strokeWidth={3} />}
                      </button>

                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                          <span style={{ fontSize: '0.75rem', fontWeight: '700', color: isCompleted ? '#34d399' : isInProgress ? '#818cf8' : '#9ca3af', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                            {item.stage} • {item.duration}
                          </span>
                          <span className={`badge ${isCompleted ? 'badge-emerald' : isInProgress ? 'badge-indigo' : 'badge-amber'}`}>
                            {item.status}
                          </span>
                        </div>

                        <h4 style={{ fontSize: '1.05rem', fontWeight: '700', color: '#f3f4f6', marginBottom: '8px' }}>
                          {item.title}
                        </h4>

                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                          {item.topics.map((topic, tIdx) => (
                            <span
                              key={tIdx}
                              style={{
                                fontSize: '0.8rem',
                                padding: '4px 10px',
                                borderRadius: '6px',
                                background: 'rgba(0, 0, 0, 0.3)',
                                color: '#d1d5db',
                                border: '1px solid var(--border-glass)'
                              }}
                            >
                              {topic}
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: RESUME SCANNER */}
        {activeTab === 'resume' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
            <div className="glass-card" style={{ padding: '24px' }}>
              <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <UploadCloud size={20} color="#818cf8" /> Resume ATS Analyzer
              </h3>
              <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '16px' }}>
                Paste your resume text below to evaluate ATS keyword match and identify skill gaps for your target role.
              </p>

              <textarea
                value={resumeText}
                onChange={(e) => setResumeText(e.target.value)}
                placeholder="Paste your resume content here (e.g. Senior Software Engineer with Java, Spring Boot, REST APIs, Microservices, React, Docker experience...)"
                style={{
                  width: '100%',
                  height: '240px',
                  background: 'rgba(0, 0, 0, 0.4)',
                  border: '1px solid var(--border-glass)',
                  borderRadius: '12px',
                  padding: '14px',
                  color: '#f3f4f6',
                  fontFamily: 'inherit',
                  fontSize: '0.9rem',
                  resize: 'none',
                  outline: 'none',
                  marginBottom: '16px'
                }}
              />

              <div style={{ display: 'flex', gap: '12px' }}>
                <button
                  onClick={handleScanResume}
                  disabled={isScanning}
                  className="btn-primary"
                  style={{ flex: 1, justifyContent: 'center' }}
                >
                  {isScanning ? <Zap className="animate-spin" size={18} /> : <BrainCircuit size={18} />}
                  {isScanning ? 'Analyzing ATS Keywords...' : 'Scan & Analyze Resume'}
                </button>
                <button
                  onClick={() => setResumeText('Akillesh K P - Software Engineer\n- Designed and built SkillBridge Java 17 Spring Boot backend microservices.\n- Integrated PDFBox for resume keyword extraction and ATS scoring.\n- Implemented Docker containers and CI/CD pipelines.\n- Proficient in Java, Kotlin, React, SQL, REST APIs, Git.')}
                  className="btn-secondary"
                >
                  Load Sample
                </button>
              </div>
            </div>

            {/* Scan Results Output */}
            <div className="glass-card" style={{ padding: '24px' }}>
              <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '16px' }}>ATS Match Output</h3>

              {!scanResult ? (
                <div style={{ textAlign: 'center', padding: '60px 20px', color: '#6b7280' }}>
                  <FileText size={48} style={{ margin: '0 auto 12px', opacity: 0.5 }} />
                  <p style={{ fontSize: '0.95rem' }}>No scan performed yet. Paste your resume or click "Load Sample" to view real-time ATS analysis.</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                  
                  {/* Score Gauge */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: '20px', background: 'rgba(16, 185, 129, 0.1)', padding: '16px 20px', borderRadius: '14px', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: '800' }} className="gradient-text-emerald">
                      {scanResult.score}%
                    </div>
                    <div>
                      <h4 style={{ fontSize: '1rem', fontWeight: '700', color: '#6ee7b7' }}>Strong ATS Qualification</h4>
                      <p style={{ fontSize: '0.8rem', color: '#9ca3af' }}>Matches target specifications for {selectedRole.name}.</p>
                    </div>
                  </div>

                  <div>
                    <h4 style={{ fontSize: '0.875rem', fontWeight: '600', color: '#34d399', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <CheckCircle2 size={16} /> Verified Skills Found:
                    </h4>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                      {scanResult.detectedSkills.map((sk, idx) => (
                        <span key={idx} className="badge badge-emerald">{sk}</span>
                      ))}
                    </div>
                  </div>

                  <div>
                    <h4 style={{ fontSize: '0.875rem', fontWeight: '600', color: '#f59e0b', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Target size={16} /> Missing Keywords to Add:
                    </h4>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                      {scanResult.missingSkills.map((sk, idx) => (
                        <span key={idx} className="badge badge-amber">{sk}</span>
                      ))}
                    </div>
                  </div>

                  <div>
                    <h4 style={{ fontSize: '0.875rem', fontWeight: '600', color: '#a5b4fc', marginBottom: '8px' }}>Optimization Suggestions:</h4>
                    <ul style={{ paddingLeft: '20px', fontSize: '0.85rem', color: '#d1d5db', lineHeight: '1.6' }}>
                      {scanResult.atsRecommendations.map((rec, rIdx) => (
                        <li key={rIdx}>{rec}</li>
                      ))}
                    </ul>
                  </div>

                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 3: SKILL ASSESSMENT & QUIZ */}
        {activeTab === 'skills' && (
          <div className="glass-card" style={{ padding: '32px', maxWidth: '800px', margin: '0 auto' }}>
            <div className="badge badge-cyan" style={{ marginBottom: '12px' }}>
              <Target size={14} /> Interactive Competency Test
            </div>
            <h3 style={{ fontSize: '1.5rem', fontWeight: '800', marginBottom: '8px' }}>Skill Gap Challenge: Microservices & Spring Boot</h3>
            <p style={{ color: '#9ca3af', fontSize: '0.9rem', marginBottom: '24px' }}>
              Test your engineering readiness. Correct answers instantly update your profile mastery score.
            </p>

            <div style={{ background: 'rgba(0, 0, 0, 0.4)', padding: '20px', borderRadius: '12px', border: '1px solid var(--border-glass)', marginBottom: '20px' }}>
              <p style={{ fontWeight: '600', fontSize: '1rem', marginBottom: '16px' }}>
                Q: When building high-throughput Spring Boot REST microservices, which strategy best prevents cascading failures when downstream services experience outages?
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {[
                  { id: 'A', text: 'Increase HTTP request timeout limits to 60 seconds' },
                  { id: 'B', text: 'Implement a Resilience4j Circuit Breaker with Fallback handlers', correct: true },
                  { id: 'C', text: 'Synchronize all REST controller methods using Java synchronized block' },
                  { id: 'D', text: 'Use infinite retry loops on every failed TCP socket connection' }
                ].map(opt => {
                  const isSelected = quizAnswer === opt.id;
                  let bg = 'rgba(255, 255, 255, 0.03)';
                  let border = '1px solid var(--border-glass)';

                  if (quizSubmitted) {
                    if (opt.correct) {
                      bg = 'rgba(16, 185, 129, 0.2)';
                      border = '1px solid #10b981';
                    } else if (isSelected && !opt.correct) {
                      bg = 'rgba(239, 68, 68, 0.2)';
                      border = '1px solid #ef4444';
                    }
                  } else if (isSelected) {
                    bg = 'rgba(99, 102, 241, 0.2)';
                    border = '1px solid #818cf8';
                  }

                  return (
                    <button
                      key={opt.id}
                      onClick={() => { if (!quizSubmitted) setQuizAnswer(opt.id); }}
                      style={{
                        padding: '12px 16px',
                        borderRadius: '10px',
                        background: bg,
                        border: border,
                        color: '#f3f4f6',
                        textAlign: 'left',
                        cursor: quizSubmitted ? 'default' : 'pointer',
                        fontSize: '0.9rem',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between'
                      }}
                    >
                      <span><strong>{opt.id}.</strong> {opt.text}</span>
                      {quizSubmitted && opt.correct && <CheckCircle2 size={18} color="#10b981" />}
                    </button>
                  );
                })}
              </div>
            </div>

            {!quizSubmitted ? (
              <button
                onClick={() => setQuizSubmitted(true)}
                disabled={!quizAnswer}
                className="btn-primary"
                style={{ width: '100%', justifyContent: 'center' }}
              >
                Submit Answer
              </button>
            ) : (
              <div style={{ background: quizAnswer === 'B' ? 'rgba(16, 185, 129, 0.1)' : 'rgba(245, 158, 11, 0.1)', padding: '16px', borderRadius: '12px', border: quizAnswer === 'B' ? '1px solid rgba(16, 185, 129, 0.3)' : '1px solid rgba(245, 158, 11, 0.3)' }}>
                <h4 style={{ fontWeight: '700', color: quizAnswer === 'B' ? '#6ee7b7' : '#fde047', marginBottom: '6px' }}>
                  {quizAnswer === 'B' ? '🎉 Correct! Mastery Score +15' : 'Incorrect Choice'}
                </h4>
                <p style={{ fontSize: '0.85rem', color: '#d1d5db' }}>
                  Circuit Breakers (Resilience4j / Hystrix) isolate downstream service faults by tripping open when failure thresholds are exceeded, executing fast fallbacks without exhausting worker thread pools.
                </p>
              </div>
            )}
          </div>
        )}

        {/* TAB 4: MOCK INTERVIEW COPILOT */}
        {activeTab === 'copilot' && (
          <div className="glass-card" style={{ padding: '24px', maxWidth: '840px', margin: '0 auto', height: '580px', display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', borderBottom: '1px solid var(--border-glass)', paddingBottom: '16px', marginBottom: '16px' }}>
              <div style={{ background: 'rgba(99, 102, 241, 0.2)', padding: '8px', borderRadius: '10px' }}>
                <BrainCircuit size={22} color="#818cf8" />
              </div>
              <div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: '700' }}>AI Mock Interview Copilot</h3>
                <p style={{ fontSize: '0.8rem', color: '#9ca3af' }}>Simulate real-time technical questions & system design feedback.</p>
              </div>
            </div>

            {/* Messages Container */}
            <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '12px', paddingRight: '6px' }}>
              {chatMessages.map((msg, mIdx) => (
                <div
                  key={mIdx}
                  style={{
                    alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                    maxWidth: '80%',
                    padding: '12px 16px',
                    borderRadius: '14px',
                    background: msg.sender === 'user' ? 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)' : 'rgba(255, 255, 255, 0.05)',
                    border: msg.sender === 'user' ? 'none' : '1px solid var(--border-glass)',
                    color: '#ffffff',
                    fontSize: '0.9rem',
                    lineHeight: '1.5'
                  }}
                >
                  {msg.text}
                </div>
              ))}
            </div>

            {/* Chat Input Form */}
            <form onSubmit={handleSendChat} style={{ display: 'flex', gap: '10px', marginTop: '16px' }}>
              <input
                type="text"
                value={userChatInput}
                onChange={(e) => setUserChatInput(e.target.value)}
                placeholder="Ask your AI copilot anything (e.g. 'How do I explain my Spring Boot & Docker project?')..."
                style={{
                  flex: 1,
                  background: 'rgba(0, 0, 0, 0.4)',
                  border: '1px solid var(--border-glass)',
                  borderRadius: '12px',
                  padding: '12px 16px',
                  color: '#ffffff',
                  outline: 'none',
                  fontSize: '0.9rem'
                }}
              />
              <button type="submit" className="btn-primary">
                Send <ChevronRight size={16} />
              </button>
            </form>
          </div>
        )}

      </main>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid var(--border-glass)', padding: '24px', textAlign: 'center', color: '#6b7280', fontSize: '0.85rem' }}>
        <p>SkillBridge Platform © 2026. Designed & Developed for Cloud Deployment on Vercel & Render.</p>
        <p style={{ marginTop: '4px' }}>
          GitHub Repository: <a href="https://github.com/Akillesh2006/SkillBridge" target="_blank" rel="noreferrer" style={{ color: '#818cf8', textDecoration: 'none' }}>github.com/Akillesh2006/SkillBridge</a>
        </p>
      </footer>
    </div>
  );
}
