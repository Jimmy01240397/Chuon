using System;
using System.IO;
using System.Windows.Forms;
using Chuon;
using ScintillaNET;

namespace ChounTranslator
{
    public partial class Form1 : Form
    {
        Scintilla richTextBox2;
        public Form1()
        {
            InitializeComponent();
            richTextBox2 = NewScintilla("cs", "");
            panel1.Controls.Add(richTextBox2);
        }

        private Scintilla NewScintilla(string Language, string text)
        {
            Scintilla Myediter;
            Myediter = new Scintilla();
            Myediter.Margins.Margin1.Width = 1;
            Myediter.Margins.Margin0.Type = MarginType.Number;
            Myediter.Margins.Margin0.Width = 0x23;
            Myediter.ConfigurationManager.Language = Language;
            Myediter.Dock = DockStyle.Fill;
            Myediter.Scrolling.ScrollBars = ScrollBars.Both;
            Myediter.ConfigurationManager.IsBuiltInEnabled = true;
            Myediter.AppendText(text);
            Myediter.UndoRedo.EmptyUndoBuffer();
            return Myediter;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            openFileDialog1.Filter = "txt檔|*.txt|所有檔案|*.*";
            openFileDialog1.Title = "選擇你要開啟的檔案";
            openFileDialog1.FileName = "";
            openFileDialog1.ShowDialog();
            if (openFileDialog1.FileName != "")
            {
                textBox1.Text = openFileDialog1.FileName;
                try
                {
                    using (FileStream fileStream = new FileStream(openFileDialog1.FileName, FileMode.Open, FileAccess.Read))
                    {
                        byte[] a = new byte[fileStream.Length];
                        fileStream.Read(a, 0, a.Length);
                        int b = 0;
                        string c = "";
                        c = StringTool.BytesToHex(a);
                        richTextBox1.Text = c;
                    }
                }
                catch (Exception ee)
                {
                    richTextBox1.Text = ee.ToString();
                }
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            saveFileDialog1.Filter = "txt檔|*.txt|所有檔案|*.*";
            saveFileDialog1.Title = "選擇你要儲存的檔案";
            saveFileDialog1.FileName = "";
            saveFileDialog1.ShowDialog();
            if (saveFileDialog1.FileName != "")
            {
                try
                {
                    textBox2.Text = saveFileDialog1.FileName;
                    byte[] q;
                    q = StringTool.HexToBytes(richTextBox1.Text);
                    using (FileStream fileStream = new FileStream(saveFileDialog1.FileName, FileMode.OpenOrCreate, FileAccess.Write))
                    {
                        fileStream.Write(q, 0, q.Length);
                    }
                }
                catch (Exception ee)
                {
                    richTextBox1.Text = ee.ToString();
                }
            }
        }

        private void button5_Click(object sender, EventArgs e)
        {
            openFileDialog1.Filter = "txt檔|*.txt|所有檔案|*.*";
            openFileDialog1.Title = "選擇你要開啟的檔案";
            openFileDialog1.FileName = "";
            openFileDialog1.ShowDialog();
            if (openFileDialog1.FileName != "")
            {
                textBox3.Text = openFileDialog1.FileName;
                richTextBox2.Text = File.ReadAllText(openFileDialog1.FileName);
            }
        }

        private void button6_Click(object sender, EventArgs e)
        {
            saveFileDialog1.Filter = "txt檔|*.txt|所有檔案|*.*";
            saveFileDialog1.Title = "選擇你要儲存的檔案";
            saveFileDialog1.FileName = "";
            saveFileDialog1.ShowDialog();
            if (saveFileDialog1.FileName != "")
            {
                textBox4.Text = saveFileDialog1.FileName;
                File.WriteAllText(saveFileDialog1.FileName, richTextBox2.Text);
            }
        }

        private void button3_Click(object sender, EventArgs e)
        {
            try
            {
                ChuonBinary vs = new ChuonBinary(StringTool.HexToBytes(richTextBox1.Text));
                richTextBox2.Text = vs.ToChuonString().ToStringWithEnter();
            }
            catch (Exception ee)
            {
                richTextBox2.Text = ee.ToString();
            }
        }

        private void button4_Click(object sender, EventArgs e)
        {
            try
            {
                richTextBox1.Text = StringTool.BytesToHex(new ChuonString(richTextBox2.Text).ToChuonBinary().ToArray());
            }
            catch (Exception ee)
            {
                richTextBox1.Text = ee.ToString();
            }
        }
    }
}
